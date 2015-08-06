package io.corbel.iam.scope;

import java.util.Map;

import io.corbel.iam.model.Scope;

/**
 * @author Alexander De Leon
 * 
 */
public interface ScopeFillStrategy {

    public Scope fillScope(Scope scope, Map<String, String> params);

}
