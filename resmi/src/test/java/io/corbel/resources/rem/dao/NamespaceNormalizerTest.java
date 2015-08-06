/*
 * Copyright (C) 2014 StarTIC
 */
package io.corbel.resources.rem.dao;

import static org.fest.assertions.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import io.corbel.resources.rem.service.DefaultNamespaceNormalizer;

/**
 * @author Alexander De Leon
 * 
 */
public class NamespaceNormalizerTest {

    private DefaultNamespaceNormalizer normalizer;

    @Before
    public void setup() {
        normalizer = new DefaultNamespaceNormalizer();
    }

    @Test
    public void testWithNamespace() {
        assertThat(normalizer.normalize("music:Album")).isEqualTo("music_Album");
    }

    @Test
    public void testWithNoNamespace() {
        assertThat(normalizer.normalize("AlbumRecord")).isEqualTo("AlbumRecord");
    }

    @Test
    public void testWithNull() {
        assertThat(normalizer.normalize(null)).isNull();
    }

}
