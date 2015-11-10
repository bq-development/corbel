package io.corbel.evci.api;

import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import javax.validation.ConstraintViolationException;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.common.collect.ImmutableMap;
import io.corbel.evci.model.Header;
import io.corbel.evci.service.EventsService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.corbel.lib.ws.auth.AuthorizationInfo;
import io.dropwizard.auth.Auth;

@Path(ApiVersion.CURRENT + "/event") public class EventResource {

    private final EventsService eventsService;
    private final ObjectMapper objectMapper;

    public EventResource(EventsService eventsService, ObjectMapper objectMapper) {
        this.eventsService = eventsService;
        this.objectMapper = objectMapper;
    }

    @POST
    @Path("/{type}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response registerJsonEvent(@Auth AuthorizationInfo authorizationInfo, @PathParam("type") String type, JsonNode event) {
        if (event != null) {
            eventsService.registerEvent(type.replaceAll(":", "."), createEworkerMessage(authorizationInfo, event));
            return Response.status(Status.ACCEPTED).build();
        }
        throw new ConstraintViolationException("Empty event", Collections.emptySet());
    }

    @POST
    @Path("/{type}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response registerFormParamsEvent(@Auth AuthorizationInfo authorizationInfo, @PathParam("type") String type, Form params, @Context Request request) {
        return Response.fromResponse(registerJsonEvent(authorizationInfo, type, getNodeParams(params))).type(MediaType.TEXT_PLAIN_TYPE).entity("[accepted]").build();
    }

    private JsonNode createEworkerMessage(AuthorizationInfo authorizationInfo, JsonNode event) {
        Header header = authorizationInfo != null ? new Header(authorizationInfo.getDomainId(), authorizationInfo.getClientId(),
                authorizationInfo.getUserId(), authorizationInfo.getTokenReader().getInfo().getDeviceId()) : new Header();
        JsonNode jsonHeader = objectMapper.convertValue(header, JsonNode.class);
        return objectMapper.createObjectNode().setAll(ImmutableMap.of("header", jsonHeader, "content", event));
    }

    private ObjectNode getNodeParams(Form params) {
        if (!params.asMap().isEmpty()) {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode node = mapper.createObjectNode();
            for (Entry<String, List<String>> entry : params.asMap().entrySet()) {
                if (entry.getValue().size() > 1) {
                    node.put(entry.getKey(), mapper.<JsonNode>valueToTree(entry.getValue()));
                } else {
                    node.put(entry.getKey(), entry.getValue().get(0));
                }
            }
            return node;
        }
        return null;
    }
}
