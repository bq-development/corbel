package io.corbel.resources.href;

import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Created by bq on 7/7/15.
 */
public class LinkBuilderTest {

    private URI uri;
    private static final String LOCALHOST_URI = "http://localhost:9091";
    private static final String PROBLEMATIC_ID = "1234/1234";
    private static final String CLEAN_ID = "12341234";
    private LinksBuilder linksBuilderMock;

    @Before
    public void setUp() throws URISyntaxException {
        uri = new URI(LOCALHOST_URI);
        linksBuilderMock = new LinksBuilder();
    }

    @Test
    public void ResourceLinksBuilderCleanIdTest() {
        LinksBuilder.ResourceLinksBuilder resourceLinksBuilder = linksBuilderMock.typeUri(uri).id(CLEAN_ID);
        resourceLinksBuilder.buildSelfLink();
        assertThat(resourceLinksBuilder.getResourceUri().toASCIIString()).isEqualTo(LOCALHOST_URI + "/" + CLEAN_ID);
    }

    @Test
    public void ResourceLinksBuilderProblematicIdTest() {
        LinksBuilder.ResourceLinksBuilder resourceLinksBuilder = linksBuilderMock.typeUri(uri).id(PROBLEMATIC_ID);
        resourceLinksBuilder.buildSelfLink();
        assertThat(resourceLinksBuilder.getResourceUri().toASCIIString()).isEqualTo(LOCALHOST_URI + "/" + PROBLEMATIC_ID);
    }
}
