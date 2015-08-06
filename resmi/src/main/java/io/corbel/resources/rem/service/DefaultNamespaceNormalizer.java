package io.corbel.resources.rem.service;

import io.corbel.resources.rem.dao.NamespaceNormalizer;

/**
 * @author Alexander De Leon
 * 
 */
public class DefaultNamespaceNormalizer implements NamespaceNormalizer {

    public String normalize(String label) {
        return label != null ? label.replace(':', '_') : null;
    }

}
