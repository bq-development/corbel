package io.corbel.oauth.api;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.corbel.lib.token.TokenGrant;
import io.corbel.lib.token.TokenInfo;
import io.corbel.lib.token.exception.TokenVerificationException;
import io.corbel.lib.token.factory.TokenFactory;
import io.corbel.lib.token.model.TokenType;
import io.corbel.lib.token.parser.TokenParser;
import io.corbel.lib.token.reader.TokenReader;
import io.corbel.lib.ws.api.error.ErrorResponseFactory;
import io.corbel.lib.ws.model.Error;
import io.corbel.oauth.model.Client;
import io.corbel.oauth.model.User;
import io.corbel.oauth.repository.UserRepository;
import io.corbel.oauth.service.ClientService;
import io.corbel.oauth.token.TokenExpireTime;

@Path(ApiVersion.CURRENT + "/oauth/token") public class TokenResource {

    private static final String AUTHORIZATION_CODE_GRANT_TYPE = "authorization_code";
    private static final Logger LOG = LoggerFactory.getLogger(TokenResource.class);
    private final TokenParser tokenParser;
    private final TokenFactory tokenFactory;
    private final ClientService clientService;
    private final UserRepository userRepository;
    private final TokenExpireTime tokenExpireTime;

    public TokenResource(TokenParser tokenParser, TokenFactory tokenFactory, ClientService clientService, UserRepository userRepository,
            TokenExpireTime tokenExpireTime) {
        this.tokenParser = tokenParser;
        this.tokenFactory = tokenFactory;
        this.clientService = clientService;
        this.userRepository = userRepository;
        this.tokenExpireTime = tokenExpireTime;
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response accessToken(@FormParam("grant_type") String grantType, @FormParam("code") String code,
            @FormParam("client_id") String clientId, @FormParam("client_secret") String clientSecret,
            @FormParam("validated_mail_required") Boolean validatedMailRequired) {

        if (StringUtils.isBlank(grantType)) {
            return ErrorResponseFactory.getInstance().missingParameter("grant_type");
        }
        if (StringUtils.isBlank(clientId)) {
            return ErrorResponseFactory.getInstance().missingParameter("client_id");
        }
        if (StringUtils.isBlank(clientSecret)) {
            return ErrorResponseFactory.getInstance().missingParameter("client_secret");
        }
        if (isNotSupportedGrantType(grantType)) {
            return ErrorResponseFactory.getInstance().badRequest(new Error("invalid_grant", grantType));
        }
        if (StringUtils.isBlank(code)) {
            return ErrorResponseFactory.getInstance().missingParameter("code");
        }

        Client client = clientService.findByName(clientId)
                .orElseThrow(() -> new WebApplicationException(ErrorResponseFactory.getInstance().unauthorized()));

        try {
            TokenReader tokenReader = tokenParser.parseAndVerify(code);

            if (TokenType.CODE != tokenReader.getInfo().getTokenType()) {
                LOG.debug("Invalid token type: " + tokenReader.getInfo().getTokenType());
                return ErrorResponseFactory.getInstance().unauthorized();
            }

            if (!tokenHasClientIdAndValidSecret(tokenReader, client, clientSecret)) {
                LOG.debug("Invalid clientId and/or secret");
                return ErrorResponseFactory.getInstance().unauthorized();
            }

            if (BooleanUtils.isTrue(validatedMailRequired) && !userHasValidatedEmail(tokenReader)) {
                return ErrorResponseFactory.getInstance().unauthorized("User need validate e-mail");
            }

            TokenInfo tokenInfo = TokenInfo.newBuilder().setType(TokenType.TOKEN).setUserId(tokenReader.getInfo().getUserId())
                    .setClientId(tokenReader.getInfo().getClientId()).setDomainId(client.getDomain()).build();

            TokenGrant token = tokenFactory.createToken(tokenInfo, tokenExpireTime.getTokenExpireTimeFromResponseType(TokenType.TOKEN));
            return Response.ok().entity(token).type(MediaType.APPLICATION_JSON_TYPE).build();

        } catch (TokenVerificationException e) {
            LOG.debug("Token verification failed", e);
        }
        return ErrorResponseFactory.getInstance().unauthorized();
    }

    private boolean userHasValidatedEmail(TokenReader tokenReader) {
        User user = userRepository.findOne(tokenReader.getInfo().getUserId());
        if (user != null) {
            return user.isEmailValidated();
        }
        return false;
    }

    private boolean tokenHasClientIdAndValidSecret(TokenReader tokenReader, Client client, String clientSecret) {
        return StringUtils.equals(tokenReader.getInfo().getClientId(), client.getName())
                && clientService.verifyClientSecret(clientSecret, client);
    }

    private boolean isNotSupportedGrantType(String grantType) {
        return !AUTHORIZATION_CODE_GRANT_TYPE.equals(grantType);
    }
}
