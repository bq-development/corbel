/*
 * Copyright (C) 2014 StarTIC
 */
package io.corbel.resources.href;

import static org.fest.assertions.api.Assertions.assertThat;

import java.net.URI;
import java.util.*;

import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * @author Alexander De Leon
 * 
 */
public class DefaultLinkGeneratorTest {

    private static final String REL = "rel";

    private static final String HREF = "href";

    private static final String LINKS = "links";

    private static final String MUSIC_COMMENTS = "music:comments";

    private static final String MUSIC_TRACKS = "music:tracks";

    private static final URI TEST_BASE_URI = URI.create("http://resources/type/");

    private static final String TYPE = "type";

    private DefaultLinkGenerator generator;

    @Before
    public void setup() {
        generator = new DefaultLinkGenerator();
    }

    @Test
    public void testGenerateSelfLinkFromId() {
        JsonObject json = new JsonObject();
        json.add("id", new JsonPrimitive("1"));
        generator.addResourceLinks(json, TEST_BASE_URI, Optional.empty());
        assertThat(json.has(LINKS)).isTrue();
        assertThat(json.get(LINKS).getAsJsonArray().get(0).getAsJsonObject().get(REL).getAsString()).isEqualTo(LinksBuilder.SELF);
        assertThat(json.get(LINKS).getAsJsonArray().get(0).getAsJsonObject().get(HREF).getAsString()).isEqualTo(TEST_BASE_URI + "1");
    }

    @Test
    public void testEmptyLinsk() {
        JsonObject json = new JsonObject();
        json.add("no_id", new JsonPrimitive("2"));
        generator.addResourceLinks(json, TEST_BASE_URI, Optional.empty());
        assertThat(json.has(LINKS)).isFalse();
    }

    @Test
    public void testGenerateRelationLinks() {
        JsonObject json = new JsonObject();
        json.add("id", new JsonPrimitive("1"));
        // when(relationSchemaServiceMock.getTypeRelations(TYPE)).thenReturn(
        // new HashSet<>(Arrays.asList(MUSIC_TRACKS, MUSIC_COMMENTS)));
        Set<String> set = new HashSet<>(Arrays.asList(MUSIC_TRACKS, MUSIC_COMMENTS));
        generator.addResourceLinks(json, TEST_BASE_URI, Optional.of(set));

        Map<String, String> relations = new HashMap<>();
        relations.put("self", TEST_BASE_URI + "1");
        relations.put(MUSIC_TRACKS, TEST_BASE_URI + "1" + "/" + MUSIC_TRACKS);
        relations.put(MUSIC_COMMENTS, TEST_BASE_URI + "1" + "/" + MUSIC_COMMENTS);

        assertThat(json.has(LINKS)).isTrue();
        JsonArray links = json.get(LINKS).getAsJsonArray();
        for (JsonElement jsonElement : links) {
            String key = jsonElement.getAsJsonObject().get(REL).getAsString();
            assertThat(relations.containsKey(key)).isEqualTo(true);
            assertThat(relations.get(key)).isEqualTo(jsonElement.getAsJsonObject().get(HREF).getAsString());
        }
    }
}
