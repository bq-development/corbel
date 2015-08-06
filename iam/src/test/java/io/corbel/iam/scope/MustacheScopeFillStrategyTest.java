package io.corbel.iam.scope;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import io.corbel.iam.model.Scope;
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
        Scope scope = mock(Scope.class);
        when(scope.getRules()).thenReturn(Collections.singleton(getRule()));

        scope = mustacheScopeFillStrategy.fillScope(scope, Collections.singletonMap("userId", "USER"));

        assertThat(scope.getRules().iterator().next().get("uri").getAsString()).isEqualTo("music:Playlist/USER");

    }

    @Test
    public void testFillWithoutRules() {
        Scope scope = mock(Scope.class);
        scope = mustacheScopeFillStrategy.fillScope(scope, Collections.singletonMap("userId", "USER"));
        assertThat(scope.getRules().size()).isEqualTo(0);
    }

    @Test
    public void testFillWithNullRules() {
        Scope scope = mock(Scope.class);
        when(scope.getRules()).thenReturn(null);
        scope = mustacheScopeFillStrategy.fillScope(scope, Collections.singletonMap("userId", "USER"));
        assertThat(scope).isEqualTo(scope);
    }

    private JsonObject getRule() {
        JsonParser parser = new JsonParser();
        return (JsonObject) parser.parse(TEST_RULE_JSON);
    }

}
