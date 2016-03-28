package io.corbel.iam.api;

import com.google.gson.JsonElement;
import io.corbel.iam.exception.DuplicatedOauthServiceIdentityException;
import io.corbel.iam.exception.IdentityAlreadyExistsException;
import io.corbel.iam.exception.UserProfileConfigurationException;
import io.corbel.iam.model.*;
import io.corbel.iam.repository.CreateUserException;
import io.corbel.iam.service.DeviceService;
import io.corbel.iam.service.DomainService;
import io.corbel.iam.service.IdentityService;
import io.corbel.iam.service.UserService;
import io.corbel.iam.utils.Message;
import io.corbel.lib.queries.builder.ResourceQueryBuilder;
import io.corbel.lib.queries.jaxrs.QueryParameters;
import io.corbel.lib.queries.request.*;
import io.corbel.lib.ws.annotation.Rest;
import io.corbel.lib.ws.auth.AuthorizationInfo;
import io.corbel.lib.ws.model.Error;
import io.dropwizard.auth.Auth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.Status;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Clock;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Alexander De Leon
 */
@Path(ApiVersion.CURRENT + "/{domain}/user") public class UserResource {

    private static final Logger LOG = LoggerFactory.getLogger(UserResource.class);
    private static final String ME = "me";

    private final UserService userService;
    private final IdentityService identityService;
    private final DomainService domainService;
    private final Clock clock;
    private final DeviceService deviceService;
    private final AggregationResultsFactory<JsonElement> aggregationResultsFactory;

    public UserResource(UserService userService, DomainService domainService, IdentityService identityService, DeviceService deviceService,
            AggregationResultsFactory<JsonElement> aggregationResultsFactory, Clock clock) {
        this.userService = userService;
        this.domainService = domainService;
        this.identityService = identityService;
        this.deviceService = deviceService;
        this.clock = clock;
        this.aggregationResultsFactory = aggregationResultsFactory;
    }

    @GET
    public Response getUsers(@PathParam("domain") String domain, @Rest QueryParameters queryParameters) {
        ResourceQuery query = queryParameters.getQuery().orElse(null);
        Pagination pagination = queryParameters.getPagination();
        Sort sort = queryParameters.getSort().orElse(null);
        Aggregation aggregation = queryParameters.getAggregation().orElse(null);

        if (queryParameters.getAggregation().isPresent()) {
            return getUsersAggregation(domain, query, aggregation);
        } else {
            List<User> users = userService.findUsersByDomain(domain, query, pagination, sort).stream().map(User::getUserProfile)
                    .collect(Collectors.toList());
            return Response.ok().type(MediaType.APPLICATION_JSON).entity(users).build();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response postUser(@PathParam("domain") String domainId, @Valid UserWithIdentity user, @Context UriInfo uriInfo,
            @Auth AuthorizationInfo authorizationInfo) {

        return domainService.getDomain(domainId).map(domain -> {
            user.setDomain(domain.getId());
            user.setScopes(domain.getDefaultScopes());
            setTracebleEntity(user, authorizationInfo);
            User createdUser;
            // The new user can only be on the domainId of the client making the request
            try {
                createdUser = userService.create(ensureNoId(user));
            } catch (CreateUserException duplicatedUser) {
                return IamErrorResponseFactory.getInstance().entityExists(Message.USER_EXISTS, duplicatedUser.getMessage());
            }
            Identity identity = user.getIdentity();
            if (identity != null) {
                try {
                    addIdentity(identity);
                } catch (Exception e) {
                    // Rollback user creation and handle error
                    userService.delete(user);
                    return handleIdentityError(e, identity);
                }
            }
            return Response.created(uriInfo.getAbsolutePathBuilder().path(createdUser.getId()).build()).build();
        }).orElseGet(() -> IamErrorResponseFactory.getInstance().invalidEntity(Message.NOT_FOUND.getMessage()));
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateUser(@PathParam("domain") String domainId, @PathParam("id") String userId,
            @Auth AuthorizationInfo authorizationInfo, User userData) {
        if (userData != null) {

            if (ME.equals(userId)) {
                userData.setScopes(null);
                userData.setGroups(null);
            }

            User user = getUserResolvingMeAndUserDomainVerifying(userId, authorizationInfo.getUserId(), domainId);

            user.updateUser(userData);

            Optional<Domain> optDomain = domainService.getDomain(domainId);

            if (!optDomain.isPresent()) {
                return IamErrorResponseFactory.getInstance().invalidEntity(Message.NOT_FOUND.getMessage());
            }

            Domain domain = optDomain.get();

            if (!domainService.scopesAllowedInDomain(user.getScopes(), domain)) {
                return IamErrorResponseFactory.getInstance().scopesNotAllowed(domain.getId());
            }
            try {
                userService.update(user);
            } catch (DuplicateKeyException e) {
                return IamErrorResponseFactory.getInstance().conflict(new Error("conflict", "The email or username already exists"));
            }
        }
        return Response.status(Status.NO_CONTENT).build();
    }

    @GET
    @Path("/{id}")
    public Response getUser(@PathParam("domain") String domainId, @PathParam("id") String userId,
            @Auth AuthorizationInfo authorizationInfo) {
        User user = getUserResolvingMeAndUserDomainVerifying(userId, authorizationInfo.getUserId(), domainId);
        return Response.ok().type(MediaType.APPLICATION_JSON).entity(user.getUserProfile()).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteUser(@PathParam("domain") String domainId, @PathParam("id") String userId,
            @Auth AuthorizationInfo authorizationInfo) {
        Optional<User> optionalUser = resolveMeIdAliases(userId, authorizationInfo.getUserId());
        optionalUser.ifPresent(user -> {
            checkingUserDomain(user, domainId);
            identityService.deleteUserIdentities(user);
            userService.delete(user);
            deviceService.deleteByUserId(user);
        });
        return Response.noContent().build();
    }

    @GET
    @Path("/{id}/avatar")
    public Response getAvatar(@PathParam("domain") String domainId, @PathParam("id") String id, @Auth AuthorizationInfo authorizationInfo) {
        User user = getUserResolvingMeAndUserDomainVerifying(id, authorizationInfo.getUserId(), domainId);
        try {
            return Response.temporaryRedirect(new URI(user.getProperties().get("avatar").toString())).build();
        } catch (NullPointerException | URISyntaxException ignored) {
            return IamErrorResponseFactory.getInstance().notfound(new Error("not_found", "User " + id + " has no avatar."));
        }
    }

    @PUT
    @Path("/{id}/disconnect")
    public Response disconnect(@PathParam("domain") String domainId, @PathParam("id") String userId,
            @Auth AuthorizationInfo authorizationInfo) {
        User user = getUserResolvingMeAndUserDomainVerifying(userId, authorizationInfo.getUserId(), domainId);
        userService.signOut(user.getId());
        return Response.noContent().build();
    }

    @DELETE
    @Path("/{id}/sessions")
    public Response deleteAllSessions(@PathParam("domain") String domainId, @PathParam("id") String userId,
            @Auth AuthorizationInfo authorizationInfo) {
        User user = getUserResolvingMeAndUserDomainVerifying(userId, authorizationInfo.getUserId(), domainId);
        userService.invalidateAllTokens(user.getId());
        return Response.noContent().build();
    }

    @POST
    @Path("/{id}/identity")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response postUserIdentity(@PathParam("domain") String domainId, @Valid Identity identity, @PathParam("id") String id,
            @Auth AuthorizationInfo authorizationInfo) {
        User user = getUserResolvingMeAndUserDomainVerifying(id, authorizationInfo.getUserId(), domainId);
        identity.setDomain(domainId);
        identity.setUserId(user.getId());
        try {
            addIdentity(identity);
            return Response.status(Status.CREATED).build();
        } catch (Exception e) {
            return handleIdentityError(e, identity);
        }
    }

    @GET
    @Path("/{id}/identity")
    public Response getUserIdentity(@PathParam("domain") String domainId, @PathParam("id") String id,
            @Auth AuthorizationInfo authorizationInfo) {
        User user = getUserResolvingMeAndUserDomainVerifying(id, authorizationInfo.getUserId(), domainId);
        return Response.ok().type(MediaType.APPLICATION_JSON).entity(identityService.findUserIdentities(user)).build();
    }

    @GET
    @Path("/{id}/profile")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserProfile(@PathParam("domain") String domainId, @PathParam("id") String id,
            @Auth AuthorizationInfo authorizationInfo) {
        User user = getUserResolvingMeAndUserDomainVerifying(id, authorizationInfo.getUserId(), domainId);
        Optional<Domain> domain = domainService.getDomain(domainId);
        if (!domain.isPresent()) {
            return IamErrorResponseFactory.getInstance().notFound();
        }
        try {
            return Optional.ofNullable(userService.getUserProfile(user, domain.get().getUserProfileFields()))
                    .map(userProfile -> Response.ok().type(MediaType.APPLICATION_JSON).entity(userProfile).build())
                    .orElseGet(() -> IamErrorResponseFactory.getInstance().notFound());
        } catch (UserProfileConfigurationException e) {
            return IamErrorResponseFactory.getInstance().serverError(new Error("misconfiguration", e.getMessage()));
        }
    }

    @PUT
    @Path("/{id}/groups")
    @Produces(MediaType.APPLICATION_JSON)
    public Response addGroupsToUser(@PathParam("domain") String domainId, @PathParam("id") String id, Set<String> groups,
            @Auth AuthorizationInfo authorizationInfo) {
        User user = getUserResolvingMeAndUserDomainVerifying(id, authorizationInfo.getUserId(), domainId);
        Optional<Domain> domain = domainService.getDomain(domainId);
        if (!domain.isPresent()) {
            return IamErrorResponseFactory.getInstance().notFound();
        }
        user.addGroups(groups);
        userService.update(user);
        return Response.noContent().build();
    }

    @DELETE
    @Path("/{id}/groups/{groupId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteGroupsToUser(@PathParam("domain") String domainId, @PathParam("id") String id,
            @PathParam("groupId") String groupId, @Auth AuthorizationInfo authorizationInfo) {
        User user = getUserResolvingMeAndUserDomainVerifying(id, authorizationInfo.getUserId(), domainId);
        Optional<Domain> domain = domainService.getDomain(domainId);
        if (!domain.isPresent()) {
            return IamErrorResponseFactory.getInstance().notFound();
        }
        user.deleteGroup(groupId);
        userService.update(user);
        return Response.noContent().build();
    }

    @GET
    @Path("/{userId}/device")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDevices(@Rest QueryParameters queryParameters, @PathParam("userId") String userId,
            @PathParam("domain") String domainId, @Auth AuthorizationInfo authorizationInfo) {
        User user = getUserResolvingMeAndUserDomainVerifying(userId, authorizationInfo.getUserId(), domainId);
        List<Device> userDevices = Optional.ofNullable(deviceService.getByUserId(user.getId(), queryParameters))
                .orElse(Collections.emptyList());
        return Response.ok().type(MediaType.APPLICATION_JSON)
                .entity(userDevices.stream().map(DeviceResponse::new).collect(Collectors.toList())).build();
    }

    @GET
    @Path("/{userId}/device/{deviceId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDevice(@PathParam("domain") String domainId, @PathParam("userId") String userId,
            @PathParam("deviceId") String deviceUid, @Auth AuthorizationInfo authorizationInfo) {
        User user = getUserResolvingMeAndUserDomainVerifying(userId, authorizationInfo.getUserId(), domainId);
        Device userDevice = deviceService.getByUidAndUserId(deviceUid, user.getId(), user.getDomain());
        if (userDevice != null) {
            return Response.ok().type(MediaType.APPLICATION_JSON).entity(new DeviceResponse(userDevice)).build();
        } else {
            return IamErrorResponseFactory.getInstance().notFound();
        }
    }

    @PUT
    @Path("/{userId}/device/{deviceId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateDevice(@PathParam("userId") String userId, @PathParam("deviceId") String deviceId,
            @PathParam("domain") String domainId, @Valid Device deviceData, @Auth AuthorizationInfo authorizationInfo,
            @Context UriInfo uriInfo) {
        User user = getUserResolvingMeAndUserDomainVerifying(userId, authorizationInfo.getUserId(), domainId);
        ensureNoId(deviceData);
        deviceData.setUid(deviceId);
        deviceData.setUserId(user.getId());
        deviceData.setDomain(domainId);
        Device storeDevice = deviceService.update(deviceData);
        return Response.created(uriInfo.getAbsolutePathBuilder().path(storeDevice.getUid()).build()).build();
    }

    @DELETE
    @Path("/{userId}/device/{deviceId}")
    public Response deleteDevice(@PathParam("domain") String domainId, @PathParam("userId") String userId,
            @PathParam("deviceId") final String deviceUid, @Auth AuthorizationInfo authorizationInfo) {
        User user = getUserResolvingMeAndUserDomainVerifying(userId, authorizationInfo.getUserId(), domainId);
        deviceService.deleteByUidAndUserId(deviceUid, user.getId(), domainId);
        return Response.status(Status.NO_CONTENT).build();
    }

    @PUT
    @Path("/me/signout")
    public Response signOut(@Auth AuthorizationInfo authorizationInfo) {
        return Optional.ofNullable(userService.findById(authorizationInfo.getUserId()))
                .filter(user -> userDomainMatchAuthorizationDomain(user, authorizationInfo.getDomainId())).map(user -> {
                    userService.signOut(user.getId(), Optional.of(authorizationInfo.getToken()));
                    return Response.noContent().build();
                }).orElseGet(() -> IamErrorResponseFactory.getInstance().notFound());
    }

    @GET
    @Path("/me/session")
    public Response getSession(@Auth AuthorizationInfo authorizationInfo) {
        return Optional.ofNullable(authorizationInfo.getToken())
                .map(token -> Response.ok().type(MediaType.APPLICATION_JSON).entity(userService.getSession(token)).build())
                .orElseGet(() -> IamErrorResponseFactory.getInstance().notFound());
    }

    @GET
    @Path("/profile")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserProfiles(@PathParam("domain") String domainId, @Auth AuthorizationInfo authorizationInfo,
            @Rest QueryParameters queryParameters) throws UserProfileConfigurationException {

        Optional<Domain> optionalDomain = domainService.getDomain(domainId);

        ResourceQuery query = queryParameters.getQuery().orElse(new ResourceQuery());

        query = filterQuery(optionalDomain, query);

        if (queryParameters.getAggregation().isPresent()) {
            return getUsersAggregation(domainId, query, queryParameters.getAggregation().get());
        }

        ResourceQuery queryFinal = query;

        List<User> profiles = optionalDomain.map(domain -> {
            try {
                return userService.findUserProfilesByDomain(domain, queryFinal, queryParameters.getPagination(),
                        queryParameters.getSort().orElse(null));
            } catch (UserProfileConfigurationException e) {
                return new LinkedList<User>();
            }
        }).orElseGet(LinkedList::new);
        return Response.ok(profiles).type(MediaType.APPLICATION_JSON).build();
    }

    @Path("/resetPassword")
    @GET
    public Response generateResetPasswordEmail(@PathParam("domain") String domainId, @QueryParam("email") String email,
            @Auth AuthorizationInfo authorizationInfo) {
        userService.sendMailResetPassword(email, authorizationInfo.getClientId(), domainId);
        return Response.noContent().build();
    }


    private <T extends Entity> T ensureNoId(T entity) {
        entity.setId(null);
        return entity;
    }

    private Response getUsersAggregation(String domainId, ResourceQuery query, Aggregation aggregation) {
        if (!AggregationOperator.$COUNT.equals(aggregation.getOperator())) {
            return IamErrorResponseFactory.getInstance()
                    .badRequest(new Error("bad_request", "Aggregator" + aggregation.getOperator() + "not supported"));
        }
        JsonElement result = aggregationResultsFactory.countResult(userService.countUsersByDomain(domainId, query));
        return Response.ok().type(MediaType.APPLICATION_JSON).entity(result).build();
    }

    private Identity addIdentity(Identity identity)
            throws IllegalOauthServiceException, IdentityAlreadyExistsException, DuplicatedOauthServiceIdentityException {

        String domainId = Optional.ofNullable(identity.getDomain())
                .orElseThrow(() -> new IllegalArgumentException("Identity \"" + identity.getId() + "\" has no domain."));

        Optional<Domain> optDomain = domainService.getDomain(domainId);

        if (!optDomain.isPresent()) {
            throw new IllegalArgumentException("Domain \"" + domainId + "\" not exists.");
        }

        optDomain.map(domain -> domainService.oAuthServiceAllowedInDomain(identity.getOauthService(), domain))
                .map(oAuthServiceAllowedInDomain -> !oAuthServiceAllowedInDomain ? null : true)
                .orElseThrow(IllegalOauthServiceException::new);

        return identityService.addIdentity(identity);
    }

    private <T extends TraceableEntity> void setTracebleEntity(T entity, AuthorizationInfo authorizationInfo) {
        String sign = authorizationInfo.getClientId();
        if (authorizationInfo.getUserId() != null) {
            sign = authorizationInfo.getUserId() + "@" + sign;
        }
        entity.setCreatedBy(sign);
        entity.setCreatedDate(Date.from(clock.instant()));
    }

    private Optional<User> resolveMeIdAliases(String id, String tokenUserId) {
        return Optional.ofNullable(userService.findById(ME.equals(id) ? tokenUserId : id));
    }

    private boolean userDomainMatchAuthorizationDomain(User user, String domainId) {
        return Objects.equals(user.getDomain(), domainId);
    }

    private void checkingUserDomain(User user, String domainId) {
        if (!userDomainMatchAuthorizationDomain(user, domainId)) {
            throw new WebApplicationException(IamErrorResponseFactory.getInstance().unauthorized("User domain mismatch"));
        }
    }

    private User getUserResolvingMeAndUserDomainVerifying(String userId, String tokenUserId, String domainId) {
        User user = resolveMeIdAliases(userId, tokenUserId)
                .orElseThrow(() -> new WebApplicationException(IamErrorResponseFactory.getInstance().notFound()));
        checkingUserDomain(user, domainId);
        return user;
    }

    private Response handleIdentityError(Exception e, Identity identity) {
        if (e instanceof IllegalOauthServiceException) {
            return IamErrorResponseFactory.getInstance().invalidOAuthService(identity.getDomain());
        }
        if (e instanceof IdentityAlreadyExistsException) {
            return IamErrorResponseFactory.getInstance().identityExists(Message.IDENTITY_EXITS, identity.getOauthId(),
                    identity.getOauthService(), identity.getDomain());
        }
        if (e instanceof DuplicatedOauthServiceIdentityException) {
            return IamErrorResponseFactory.getInstance().oauthServiceDuplicated(Message.DUPLICATED_OAUTH_SERVICE_IDENTITY,
                    identity.getUserId(), identity.getOauthService(), identity.getDomain());
        }
        if (e instanceof IllegalArgumentException) {
            return IamErrorResponseFactory.getInstance().invalidArgument(e.getMessage());
        }
        LOG.error("Unexpected exception", e);
        return IamErrorResponseFactory.getInstance().serverError(e);
    }

    private ResourceQuery filterQuery(Optional<Domain> optionalDomain, ResourceQuery query) {
        Set<String> filters = query.getFilters();
        Set<String> difference = optionalDomain.map(domain -> {
            filters.removeAll(domain.getUserProfileFields());
            return filters;
        }).orElseGet(HashSet::new);

        if (!difference.isEmpty()) {
            query = new ResourceQueryBuilder().add("_notExistent", "").build();
        }
        return query;
    }

    @SuppressWarnings("serial") private static class IllegalOauthServiceException extends Exception {}
}
