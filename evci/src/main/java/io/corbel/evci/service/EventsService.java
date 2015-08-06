package io.corbel.evci.service;

import com.fasterxml.jackson.databind.JsonNode;

public interface EventsService {

	void registerEvent(String type, JsonNode event);

}
