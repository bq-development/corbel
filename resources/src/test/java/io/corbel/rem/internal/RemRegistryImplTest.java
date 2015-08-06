/*
 * Copyright (C) 2013 StarTIC
 */
package io.corbel.rem.internal;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import io.corbel.resources.rem.Rem;

/**
 * @author Alexander De Leon
 * 
 */
public class RemRegistryImplTest {

    private InMemoryRemRegistry registry;

    @Before
    public void setup() {
        registry = new InMemoryRemRegistry();
    }

    @Test
    public void testUriPatternSufixMatch() {
        Rem rem = mock(Rem.class);
        registry.registerRem(rem, "music:Album/.*");
        assertThat(registry.getRem("music:Album/123", Arrays.asList(MediaType.APPLICATION_JSON), HttpMethod.GET, null)).isSameAs(rem);
    }

    @Test
    public void testUriPatternSufixMatchButExcludeRem() {
        Rem rem = mock(Rem.class);
        registry.registerRem(rem, "music:Album/.*");
        assertThat(registry.getRem("music:Album/123", Arrays.asList(MediaType.APPLICATION_JSON), HttpMethod.GET, Arrays.asList(rem)))
                .isNull();
    }

    @Test
    public void testUriPatternPrefixMatch() {
        Rem rem = mock(Rem.class);
        registry.registerRem(rem, "music:Album/.*/music:tracks");
        assertThat(registry.getRem("music:Album/123/music:tracks", Arrays.asList(MediaType.APPLICATION_JSON), HttpMethod.GET, null))
                .isSameAs(rem);
    }

    @Test
    public void testUriPatternPrefixMatchButExcludeRem() {
        Rem rem = mock(Rem.class);
        registry.registerRem(rem, "music:Album/.*/music:tracks");
        assertThat(
                registry.getRem("music:Album/123/music:tracks", Arrays.asList(MediaType.APPLICATION_JSON), HttpMethod.GET,
                        Arrays.asList(rem))).isNull();
    }

    @Test
    public void testSubsumedUriPatternPrefixes() {
        Rem rem1 = mock(Rem.class);
        Rem rem2 = mock(Rem.class);
        registry.registerRem(rem1, "music:Album/.*");
        registry.registerRem(rem2, "music:Album/7digital-.*");
        assertThat(registry.getRem("music:Album/7digital-123", Arrays.asList(MediaType.APPLICATION_JSON), HttpMethod.GET, null)).isSameAs(
                rem2);
        assertThat(registry.getRem("music:Album/providerX-123", Arrays.asList(MediaType.APPLICATION_JSON), HttpMethod.GET, null)).isSameAs(
                rem1);
    }

    @Test
    public void testSubsumedUriPatternPrefixes2() {
        Rem rem1 = mock(Rem.class);
        Rem rem2 = mock(Rem.class);
        registry.registerRem(rem2, "music:Album/7digital-.*");
        registry.registerRem(rem1, "music:Album/.*");
        assertThat(registry.getRem("music:Album/7digital-123", Arrays.asList(MediaType.APPLICATION_JSON), HttpMethod.GET, null)).isSameAs(
                rem2);
        assertThat(registry.getRem("music:Album/providerX-123", Arrays.asList(MediaType.APPLICATION_JSON), HttpMethod.GET, null)).isSameAs(
                rem1);
    }

    @Test
    public void testSingleMediaType() {
        Rem rem = mock(Rem.class);
        registry.registerRem(rem, "music:Album/.*", MediaType.APPLICATION_JSON);
        assertThat(registry.getRem("music:Album/123", Arrays.asList(MediaType.APPLICATION_JSON), HttpMethod.GET, null)).isSameAs(rem);
        assertThat(registry.getRem("music:Album/123", Arrays.asList(MediaType.APPLICATION_ATOM_XML), HttpMethod.GET, null)).isNull();
    }

    @Test
    public void testMatchingMediaType() {
        Rem rem = mock(Rem.class);
        registry.registerRem(rem, "music:Album/.*", new MediaType("image", "*"));
        assertThat(registry.getRem("music:Album/123", Arrays.asList(MediaType.IMAGE_PNG), HttpMethod.GET, null)).isSameAs(rem);
        assertThat(registry.getRem("music:Album/123", Arrays.asList(MediaType.IMAGE_JPEG), HttpMethod.GET, null)).isSameAs(rem);
        assertThat(registry.getRem("music:Album/123", Arrays.asList(MediaType.APPLICATION_ATOM_XML), HttpMethod.GET, null)).isNull();
    }

    @Test
    public void testSingleMethod() {
        Rem rem = mock(Rem.class);
        registry.registerRem(rem, "music:Album/.*", MediaType.APPLICATION_JSON, HttpMethod.GET);
        assertThat(registry.getRem("music:Album/123", Arrays.asList(MediaType.APPLICATION_JSON), HttpMethod.GET, null)).isSameAs(rem);
        assertThat(registry.getRem("music:Album/123", Arrays.asList(MediaType.ALL), HttpMethod.POST, null)).isNull();
    }

    @Test
    public void testMultipleMethod() {
        Rem rem = mock(Rem.class);
        registry.registerRem(rem, "music:Album/.*", MediaType.APPLICATION_JSON, HttpMethod.GET);
        assertThat(registry.getRem("music:Album/123", Arrays.asList(MediaType.APPLICATION_JSON), HttpMethod.GET, null)).isSameAs(rem);
        assertThat(registry.getRem("music:Album/123", Arrays.asList(MediaType.ALL), HttpMethod.POST, null)).isNull();
        Rem rem2 = mock(Rem.class);
        registry.registerRem(rem2, "music:Album/.*", MediaType.APPLICATION_JSON, HttpMethod.POST);
        assertThat(registry.getRem("music:Album/123", Arrays.asList(MediaType.APPLICATION_JSON), HttpMethod.POST, null)).isSameAs(rem2);
    }

    @Test
    public void testUnmatch() {
        Rem rem = mock(Rem.class);
        registry.registerRem(rem, "music:Album/.*");
        assertThat(registry.getRem("music:Track/123", Arrays.asList(MediaType.APPLICATION_JSON), HttpMethod.GET, null)).isNull();
    }

    @Test
    public void testDubleRegistry() {
        Rem rem1 = mock(Rem.class);
        Rem rem2 = mock(Rem.class);
        registry.registerRem(rem1, "music:Album/.*", MediaType.APPLICATION_JSON);
        registry.registerRem(rem2, "music:Album/.*", MediaType.APPLICATION_ATOM_XML);
        assertThat(registry.getRem("music:Album/123", Arrays.asList(MediaType.APPLICATION_JSON), HttpMethod.GET, null)).isSameAs(rem1);
        assertThat(registry.getRem("music:Album/123", Arrays.asList(MediaType.APPLICATION_ATOM_XML), HttpMethod.GET, null)).isSameAs(rem2);
    }

    @Test
    public void testTwoDistinticRegistry() {
        Rem rem1 = mock(Rem.class);
        Rem rem2 = mock(Rem.class);
        registry.registerRem(rem1, "music:Album/.*", MediaType.APPLICATION_JSON, HttpMethod.GET);
        registry.registerRem(rem2, "music:Artist/.*", MediaType.APPLICATION_JSON, HttpMethod.GET);
        assertThat(registry.getRem("music:Album/123", Arrays.asList(MediaType.APPLICATION_JSON), HttpMethod.GET, null)).isSameAs(rem1);
        assertThat(registry.getRem("music:Artist/123", Arrays.asList(MediaType.APPLICATION_JSON), HttpMethod.GET, null)).isSameAs(rem2);
    }
}
