package io.corbel.rem.internal;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Rubén Carrasco
 * 
 */
public class InMemoryRelationRegistryTest {

    private static final String TEST_TYPE = "testType";
    private static final String REL1 = "rel1";
    private static final String REL2 = "rel2";
    private static final String FIELD1 = "field1";
    private static final String FIELD2 = "field2";
    private static final String FIELD3 = "field3";
    private static final String FIELD4 = "field4";
    private static final Set<String> TEST_RELS = new HashSet<String>(Arrays.asList(REL1, REL2));
    private static final Set<String> TEST_FIELDS1 = new HashSet<String>(Arrays.asList(FIELD1, FIELD2));
    private static final Set<String> TEST_FIELDS2 = new HashSet<String>(Arrays.asList(FIELD3, FIELD4));
    private InMemoryRelationRegistry registry;

    @Before
    public void setup() {
        registry = new InMemoryRelationRegistry();
        registry.addRelationFields(TEST_TYPE, REL1, TEST_FIELDS1);
        registry.addRelationFields(TEST_TYPE, REL2, TEST_FIELDS2);
    }

    @Test
    public void testTypeRelation() {
        assertThat(registry.getTypeRelations(TEST_TYPE)).isEqualTo(TEST_RELS);
    }

    @Test
    public void testRelationFields() {
        assertThat(registry.getRelationFields(TEST_TYPE, REL1)).isEqualTo(TEST_FIELDS1);
        assertThat(registry.getRelationFields(TEST_TYPE, REL2)).isEqualTo(TEST_FIELDS2);
    }

}
