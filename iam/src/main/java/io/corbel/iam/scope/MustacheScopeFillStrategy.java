package io.corbel.iam.scope;

import java.io.StringReader;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.codehaus.groovy.runtime.StringBufferWriter;

import io.corbel.iam.model.Scope;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * @author Alexander De Leon
 * 
 */
public class MustacheScopeFillStrategy implements ScopeFillStrategy {

    private final MustacheFactory mustacheFactory = new DefaultMustacheFactory();

    @Override
    public Scope fillScope(Scope scope, Map<String, String> params) {
        if (scope.getRules() == null) {
            return scope;
        }
        Set<JsonObject> rules = scope.getRules();
        Set<JsonObject> filledRules = new HashSet<JsonObject>(rules.size());
        for (JsonObject rule : rules) {
            filledRules.add(fill(rule, params));
        }

        return new Scope(scope.getId(), scope.getType(), scope.getAudience(), scope.getScopes(), filledRules, scope.getParameters());
    }

    private JsonObject fill(JsonObject rule, Map<String, String> params) {
        String ruleString = rule.toString();
        Mustache mustache = mustacheFactory.compile(new StringReader(ruleString), ruleString);
        StringBuffer buffer = new StringBuffer();
        StringBufferWriter writer = new StringBufferWriter(buffer);
        mustache.execute(writer, params);

        JsonParser parser = new JsonParser();
        return (JsonObject) parser.parse(buffer.toString());
    }
}
