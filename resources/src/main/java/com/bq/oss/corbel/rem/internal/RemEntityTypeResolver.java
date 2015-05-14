package com.bq.oss.corbel.rem.internal;

import com.bq.oss.corbel.resources.rem.Rem;

/**
 * @author Alexander De Leon
 * 
 */
public interface RemEntityTypeResolver {

    Class<?> getEntityType(Rem<?> rem);

}
