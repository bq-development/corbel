package io.corbel.resources.rem.dao;

import io.corbel.resources.rem.model.RestorResourceUri;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * @author Alberto J. Rubio
 */
public class DefaultKeyNormalizerTest {

	private DefaultKeyNormalizer normalizer;

	@Before
	public void setup() {
		normalizer = new DefaultKeyNormalizer();
	}

	@Test
	public void testRawResource() {
		assertThat(normalizer.normalize(new RestorResourceUri("domain", MediaType.IMAGE_PNG.toString(), "test", "imageId"))).isEqualTo("domain/test/imageId.image_png");
	}

	@Test
	public void testNormalizedResource() {
		assertThat(normalizer.normalize(new RestorResourceUri("domain", MediaType.IMAGE_PNG.toString(), "test", "test/imageId.image_png"))).isEqualTo("domain/test/imageId.image_png");
	}
}
