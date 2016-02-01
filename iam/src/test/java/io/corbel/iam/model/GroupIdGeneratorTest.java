package io.corbel.iam.model;

import com.google.common.base.Joiner;
import io.corbel.lib.ws.digest.Digester;
import io.corbel.lib.ws.digest.DigesterFactory;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Alexander De Leon
 * 
 */
public class GroupIdGeneratorTest {

    private static final char SEPARATOR = ':';
    private static final String TEST_ID = "hash_id";
    private static final String TEST_DOMAIN = "domain";
    private static final String TEST_NAME = "name";

    @Test
    public void testIdGeneration() {
        Digester digesterMock = mock(Digester.class);
        GroupIdGenerator generator = new GroupIdGenerator(digesterMock);
        Group client = new Group();
        client.setDomain(TEST_DOMAIN);
        client.setName(TEST_NAME);
        when(digesterMock.digest(TEST_DOMAIN + ":" + TEST_NAME)).thenReturn(TEST_ID);
        assertThat(generator.generateId(client)).isEqualTo(TEST_ID);
    }

    @Test
    public void testEqualIdGeneration(){
        Group client = new Group();
        client.setDomain(TEST_DOMAIN);
        client.setName(TEST_NAME);

        Digester digester = DigesterFactory.murmur3_32();
        String idA = digester.digest(getIdString(client));

        Group client2 = new Group();
        client2.setDomain(TEST_DOMAIN);
        client2.setName(TEST_NAME);

        digester = DigesterFactory.murmur3_32();
        String idB = digester.digest(getIdString(client));
        assertThat(idA).isEqualTo(idB);
    }

    private String getIdString(Group entity){
        return Joiner.on(SEPARATOR).join(entity.getDomain(), entity.getName());
    }

}
