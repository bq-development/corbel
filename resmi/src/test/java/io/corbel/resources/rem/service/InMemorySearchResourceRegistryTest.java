package io.corbel.resources.rem.service;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import io.corbel.resources.rem.model.SearchResource;

/**
 * @author Francisco Sanchez
 */
public class InMemorySearchResourceRegistryTest {

    private static final String DOMAIN1 = "DOMAIN1";
    private static final String DOMAIN2 = "DOMAIN2";

    private InMemorySearchableFieldsRegistry inMemorySearchableFieldsRegistry;

    @Before
    public void setup() {
        inMemorySearchableFieldsRegistry = new InMemorySearchableFieldsRegistry();
    }

    @Test
    public void registryTest() {
        Set<String> s1Fields = new HashSet(Arrays.asList("s1.1", "s1.2"));
        Set<String> s2Fields = new HashSet(Arrays.asList("s2.1", "s2.2"));
        Set<String> s3Fields = new HashSet(Arrays.asList("s3.1", "s3.2"));

        SearchResource s1 = new SearchResource(DOMAIN1, "s1", s1Fields);
        SearchResource s2 = new SearchResource(DOMAIN2, "s2", s2Fields);
        SearchResource s3 = new SearchResource(DOMAIN1, "s3", s3Fields);
        SearchResource s3Mod = new SearchResource(DOMAIN1, "s3", s1Fields);

        inMemorySearchableFieldsRegistry.addFields(s1);
        inMemorySearchableFieldsRegistry.addFields(s2);
        inMemorySearchableFieldsRegistry.addFields(s3);
        inMemorySearchableFieldsRegistry.addFields(s3Mod);


        assertThat(inMemorySearchableFieldsRegistry.getFieldsFromType(DOMAIN1, "s1")).isEqualTo(s1Fields);
        assertThat(inMemorySearchableFieldsRegistry.getFieldsFromType(DOMAIN1,"s2")).isEmpty();
        assertThat(inMemorySearchableFieldsRegistry.getFieldsFromType(DOMAIN2,"s2")).isEqualTo(s2Fields);
        assertThat(inMemorySearchableFieldsRegistry.getFieldsFromType(DOMAIN1,"s3")).isEqualTo(s1Fields);
        assertThat(inMemorySearchableFieldsRegistry.getFieldsFromType(DOMAIN1,"s4")).isEmpty();

    }

}
