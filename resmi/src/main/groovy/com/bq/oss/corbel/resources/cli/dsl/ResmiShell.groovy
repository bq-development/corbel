package com.bq.oss.corbel.resources.cli.dsl

import com.bq.oss.corbel.resources.rem.model.ResourceUri
import com.bq.oss.corbel.resources.rem.model.SearchableFields
import com.bq.oss.corbel.resources.rem.service.ResmiService
import com.bq.oss.lib.cli.console.Description
import com.bq.oss.lib.cli.console.Shell
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.springframework.data.mongodb.core.index.Index

@Shell("resmi")
class ResmiShell {

    ResmiService resmiService

    @Description("Creates a mongo expiration index named \"_expireAt\" on the specified collection .")
    def ensureExpireIndex(String collection) {
        resmiService.ensureExpireIndex(new ResourceUri(collection))
    }

    @Description("Creates a mongo index on the specified collection. See <> for documentation on index syntax")
    def ensureIndex(String collection, Index index) {
        resmiService.ensureIndex(new ResourceUri(collection), index)
    }

    @Description("Creates a mongo index on the specified relation. See <> for documentation on index syntax")
    def ensureIndex(String collection, String relation, Index index) {
        resmiService.ensureIndex(new ResourceUri(collection).setRelation(relation), index)
    }

    @Description("Upsert a resource in RESMI.")
    def upsert(String type, JsonObject json) {
        resmiService.saveResource(new ResourceUri(type), json, Optional.empty())
    }

    @Description("Construct a json object from string. Example: resmi.json('{\"a\", \"b\"}')")
    def json(String json) {
        new JsonParser().parse(json)
    }

    @Description("Full text search fields in a type.")
    def searchableFields(String type, String... fields) {
        assert type: "type is required"
        assert fields: "fields is required"
        resmiService.addSearchableFields(new SearchableFields(type, fields.collect().toSet()))
    }

    @Description("Full text search fields in a type.")
    def searchableRelationFields(String type, String relation, String... fields) {
        assert type: "type is required"
        assert relation: "relation is required"
        assert fields: "fields is required"
        resmiService.addSearchableFields(new SearchableFields(type, relation, fields.collect().toSet()))
    }

    def index = IndexBuilder.index
}
