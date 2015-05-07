package com.bq.oss.corbel.iam.scope;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import com.bq.oss.corbel.iam.model.Scope;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * @author Alexander De Leon
 * 
 */
public class MustacheScopeFillStrategyTest {

    private static final String TEST_RULE_JSON = "{\"uri\":\"music:Playlist/{{userId}}\"}";

    private MustacheScopeFillStrategy mustacheScopeFillStrategy;

    @Before
    public void setup() {
        mustacheScopeFillStrategy = new MustacheScopeFillStrategy();
    }

    @Test
    public void testFill() {
        Scope scope = new Scope();
        scope.setRules(Collections.singleton(getRule()));

        scope = mustacheScopeFillStrategy.fillScope(scope, Collections.singletonMap("userId", "USER"));

        assertThat(scope.getRules().iterator().next().get("uri").getAsString()).isEqualTo("music:Playlist/USER");

    }

    @Test
    public void testFillWithoutRules() {
        Scope scope = new Scope();
        scope = mustacheScopeFillStrategy.fillScope(scope, Collections.singletonMap("userId", "USER"));
        assertThat(scope.getRules().size()).isEqualTo(0);
    }

    @Test
    public void testFillWithNullRules() {
        Scope scope = new Scope();
        scope.setRules(null);
        scope = mustacheScopeFillStrategy.fillScope(scope, Collections.singletonMap("userId", "USER"));
        assertThat(scope).isEqualTo(scope);
    }

    private JsonObject getRule() {
        JsonParser parser = new JsonParser();
        return (JsonObject) parser.parse(TEST_RULE_JSON);
    }

}
