package com.bq.oss.corbel.resources.rem.dao;

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
	public void test() {
		assertThat(normalizer.normalize(MediaType.IMAGE_PNG, "test", "imageId")).isEqualTo("test/imageId.image_png");
	}

}
