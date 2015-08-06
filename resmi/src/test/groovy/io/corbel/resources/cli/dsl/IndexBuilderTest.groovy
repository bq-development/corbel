package io.corbel.resources.cli.dsl

import org.junit.Test
import org.springframework.data.mongodb.core.index.Index

import java.util.concurrent.TimeUnit

import static org.junit.Assert.assertEquals

public class IndexBuilderTest {

    private static final String TEST_FIELD = "field"

    @Test
    void testBasicIndex() {
        Index index = IndexBuilder.index({ on TEST_FIELD })
        assertEquals(1, index.getIndexKeys().get(TEST_FIELD))
    }

    @Test
    void testBasicIndexWithDirection() {
        Index index = IndexBuilder.index({ on(TEST_FIELD, 'desc') })
        assertEquals(-1, index.getIndexKeys().get(TEST_FIELD))
    }

    @Test
    void testIndexOptionsBackground() {
        Index index = IndexBuilder.index({ on(TEST_FIELD, 'desc'); background() })
        assertEquals(-1, index.getIndexKeys().get(TEST_FIELD))
        assertEquals(true, index.getIndexOptions().get("background"))
    }

    @Test
    void testIndexOptionsUnique() {
        Index index = IndexBuilder.index({ on(TEST_FIELD, 'desc'); unique() })
        assertEquals(-1, index.getIndexKeys().get(TEST_FIELD))
        assertEquals(true, index.getIndexOptions().get("unique"))
    }

    @Test
    void testIndexOptionsExpire() {
        IndexBuilder builder = new IndexBuilder()
        Index index = builder.index({ on(TEST_FIELD, 'desc'); expire(100) })
        assertEquals(-1, index.getIndexKeys().get(TEST_FIELD))
        assertEquals(100, index.getIndexOptions().get("expireAfterSeconds"))
    }

    @Test
    void testIndexOptionsExpireInMinutes() {
        IndexBuilder builder = new IndexBuilder()
        Index index = builder.index({ on(TEST_FIELD, 'desc'); expire(1, TimeUnit.MINUTES) })
        assertEquals(-1, index.getIndexKeys().get(TEST_FIELD))
        assertEquals(60, index.getIndexOptions().get("expireAfterSeconds"))
    }

    @Test
    void testConpoundIndex() {
        IndexBuilder builder = new IndexBuilder()
        Index index = builder.index({ on(TEST_FIELD, 'desc'); on(TEST_FIELD + "2", 'asc') })
        assertEquals(-1, index.getIndexKeys().get(TEST_FIELD))
        assertEquals(1, index.getIndexKeys().get(TEST_FIELD + "2"))
    }
}
