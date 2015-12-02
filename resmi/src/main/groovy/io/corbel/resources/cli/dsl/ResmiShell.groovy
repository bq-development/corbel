package io.corbel.resources.cli.dsl

import io.corbel.lib.cli.console.Description
import io.corbel.lib.cli.console.Shell
import io.corbel.resources.rem.model.ResourceUri
import io.corbel.resources.rem.model.SearchResource
import io.corbel.resources.rem.search.ElasticSearchResmiSearch
import io.corbel.resources.rem.search.ElasticSearchService
import io.corbel.resources.rem.search.ResmiSearch
import io.corbel.resources.rem.service.ResmiService

import org.springframework.data.mongodb.core.index.Index

import com.google.gson.JsonObject
import com.google.gson.JsonParser

@Shell("resmi")
class ResmiShell {

    ResmiService resmiService
    ElasticSearchService elasticSearchService
    ResmiSearch resmiSearch

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
        resmiService.addSearchableFields(new SearchResource(type, fields.collect().toSet()))
    }

    @Description("Full text search fields in a type.")
    def searchableRelationFields(String type, String relation, String... fields) {
        assert type: "type is required"
        assert relation: "relation is required"
        assert fields: "fields is required"
        resmiService.addSearchableFields(new SearchResource(type, relation, fields.collect().toSet()))
    }

    @Description("Raw creation of an index with settings.")
    def createIndex(String name, String settings) {
        assert name: "name is required"
        assert settings: "settings is required"
        elasticSearchService.createIndex(name, settings)
    }

    @Description("Defines an resmi index with settings if not exists.")
    def upsertIndex(String name, String settings) {
        assert name: "name is required"
        assert settings: "settings is required"
        resmiSearch.upsertResmiIndex(name, settings)
    }

    @Description("Defines a full text search mapping for a type, creating its resmi index if not exists.")
    def setMapping(String type, String mapping) {
        assert type: "type is required"
        assert mapping: "mapping is required"
        resmiSearch.upsertResmiIndex(type)
        elasticSearchService.setupMapping(ElasticSearchResmiSearch.RESMI_INDEX_PREFIX + type, type, mapping)
    }

    @Description("Adds a full text search template")
    def addTemplate(String name, Map<String, Object> template) {
        assert name: "name is required"
        assert template: "template is required"
        elasticSearchService.addTemplate(name, template)
    }

    @Description("Adds an alias to an index")
    def addAlias(String index, String alias) {
        assert index: "index is required"
        assert alias: "alias is required"
        elasticSearchService.addAlias(index, alias)
    }

    @Description("Removes an alias from an index")
    def removeAlias(String index, String alias) {
        assert index: "index is required"
        assert alias: "alias is required"
        elasticSearchService.removeAlias(index, alias)
    }


    def index = IndexBuilder.index
}
