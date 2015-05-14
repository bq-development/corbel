package com.bq.oss.corbel.resources.rem.service;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.bq.oss.corbel.resources.rem.model.SearchableFields;

/**
 * @author Francisco Sanchez
 */
public class InMemorySearchableFieldsRegistryTest {

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

        SearchableFields s1 = new SearchableFields("s1", s1Fields);
        SearchableFields s2 = new SearchableFields("s2", s2Fields);
        SearchableFields s3 = new SearchableFields("s3", s3Fields);
        SearchableFields s3Mod = new SearchableFields("s3", s1Fields);

        inMemorySearchableFieldsRegistry.addFields(s1);
        inMemorySearchableFieldsRegistry.addFields(s2);
        inMemorySearchableFieldsRegistry.addFields(s3);
        inMemorySearchableFieldsRegistry.addFields(s3Mod);


        assertThat(inMemorySearchableFieldsRegistry.getFieldsFromType("s1")).isEqualTo(s1Fields);
        assertThat(inMemorySearchableFieldsRegistry.getFieldsFromType("s2")).isEqualTo(s2Fields);
        assertThat(inMemorySearchableFieldsRegistry.getFieldsFromType("s3")).isEqualTo(s1Fields);
        assertThat(inMemorySearchableFieldsRegistry.getFieldsFromType("s4")).isEmpty();

    }

}
