/*
 * Copyright (C) 2014 StarTIC
 */
package io.corbel.resources.rem.dao;

/**
 * Normalizes namespaced labels into syntactic valid labels for the RESMI db.
 * 
 * @author Alexander De Leon
 * 
 */
public interface NamespaceNormalizer {

    String normalize(String label);

}
