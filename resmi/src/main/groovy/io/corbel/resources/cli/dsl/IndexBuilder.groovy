package io.corbel.resources.cli.dsl

import org.springframework.data.domain.Sort.Direction
import org.springframework.data.mongodb.core.index.Index

class IndexBuilder {

    static def index = { Closure index ->
        IndexWrapper indexObj = new IndexWrapper()
        index.delegate = indexObj
        index.call()
        return indexObj
    }

    private static class IndexWrapper extends Index {
        Index on(String field) {
            return on(field, Direction.ASC)
        }

        Index on(String field, String direction) {
            return on(field, Enum.valueOf(Direction, direction.toUpperCase()))
        }
    }
}
