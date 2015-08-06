package io.corbel.rem.internal;

import io.corbel.resources.rem.Rem;

/**
 * @author Alexander De Leon
 * 
 */
public interface RemEntityTypeResolver {

    Class<?> getEntityType(Rem<?> rem);

}
