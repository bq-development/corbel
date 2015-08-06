package io.corbel.rem.internal;

import io.corbel.resources.rem.Rem;

/**
 * @author Alexander De Leon
 * 
 */
public class DefaultRemEntityTypeResolver implements RemEntityTypeResolver {

    @Override
    public Class<?> getEntityType(Rem<?> rem) {
        return rem.getType();
    }

}
