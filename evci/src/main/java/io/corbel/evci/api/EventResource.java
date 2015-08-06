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

import io.corbel.evci.service.EventsService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Path(ApiVersion.CURRENT + "/event") public class EventResource {

    private final EventsService eventsService;

    public EventResource(EventsService eventsService) {
        this.eventsService = eventsService;
    }

    @POST
    @Path("/{type}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response registerJsonEvent(@PathParam("type") String type, JsonNode event) {
        if (event != null) {
            eventsService.registerEvent(type.replaceAll(":", "."), event);
            return Response.status(Status.ACCEPTED).build();
        }
        throw new ConstraintViolationException("Empty event", Collections.emptySet());
    }

    @POST
    @Path("/{type}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response registerFormParamsEvent(@PathParam("type") String type, Form params, @Context Request request) {
        Response response = Response.fromResponse(registerJsonEvent(type, getNodeParams(params))).type(MediaType.TEXT_PLAIN_TYPE)
                .entity("[accepted]").build();
        return response;
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
