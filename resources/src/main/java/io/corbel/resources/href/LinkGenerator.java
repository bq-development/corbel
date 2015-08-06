package io.corbel.resources.href;

import java.net.URI;
import java.util.Optional;
import java.util.Set;

import com.google.gson.JsonObject;

/**
 * @author Alexander De Leon
 * 
 */
public interface LinkGenerator {

    JsonObject addResourceLinks(JsonObject resource, URI typeUri, Optional<Set<String>> relations);

}
