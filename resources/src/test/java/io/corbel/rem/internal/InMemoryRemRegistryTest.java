package io.corbel.rem.internal;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import io.corbel.resources.rem.Rem;

public class InMemoryRemRegistryTest {

    @Before
    public void setUp() throws Exception {}

    @Test
    public void test() {
        InMemoryRemRegistry registry = new InMemoryRemRegistry();
        Rem rem1 = mock(Rem.class);
        registry.registerRem(rem1, ".*", MediaType.parseMediaType("image/*"), HttpMethod.GET);
        Rem rem2 = mock(Rem.class);
        registry.registerRem(rem2, "^(?!xxx$).*", MediaType.parseMediaType("image/jpeg"), HttpMethod.GET);

        Rem selected = registry.getRem("foo.com", Arrays.asList(MediaType.parseMediaType("image/jpeg")), HttpMethod.GET, null);
        assertThat(selected).isEqualTo(rem2);
    }

}