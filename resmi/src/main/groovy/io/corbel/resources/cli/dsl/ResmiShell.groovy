package io.corbel.resources.cli.dsl

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import io.corbel.lib.cli.console.Description
import io.corbel.lib.cli.console.Shell
import io.corbel.resources.rem.model.ResourceUri
import io.corbel.resources.rem.model.SearchResource
import io.corbel.resources.rem.search.ElasticSearchService
import io.corbel.resources.rem.search.ResmiSearch
import io.corbel.resources.rem.service.ResmiService
import org.springframework.data.mongodb.core.index.IndexDefinition

@Shell("resmi")
class ResmiShell {

    ResmiService resmiService
    ElasticSearchService elasticSearchService
    ResmiSearch resmiSearch

    @Description("Creates a mongo expiration index named \"_expireAt\" on the specified collection .")
    def ensureExpireIndex(String domain, String collection) {
        assert domain: "domain is required"
        assert collection: "collection is required"
        resmiService.ensureExpireIndex(new ResourceUri(domain, collection))
    }

    @Description("Creates a mongo index on the specified collection. See <> for documentation on index syntax")
    def ensureIndex(String domain, String collection, IndexDefinition indexDefinition) {
        assert domain: "domain is required"
        assert collection: "collection is required"
        assert indexDefinition: "indexDefinition is required"
        resmiService.ensureIndex(new ResourceUri(domain, collection), indexDefinition)
    }

    @Description("Creates a mongo index on the specified relation. See <> for documentation on index syntax")
    def ensureIndex(String domain, String collection, String relation, IndexDefinition indexDefinition) {
        assert domain: "domain is required"
        assert collection: "collection is required"
        assert relation: "relation is required"
        assert indexDefinition: "indexDefinition is required"
        resmiService.ensureIndex(new ResourceUri(domain, collection).setRelation(relation), indexDefinition)
    }

    @Description("Upsert a resource in RESMI.")
    def upsert(String domain, String collection, JsonObject json) {
        assert domain: "domain is required"
        assert collection: "collection is required"
        resmiService.saveResource(new ResourceUri(domain, collection), json, Optional.empty())
    }

    @Description("Construct a json object from string. Example: resmi.json('{\"a\", \"b\"}')")
    def json(String json) {
        new JsonParser().parse(json)
    }

    @Description("Full text search fields in a type.")
    def searchableFields(String domain, String type, String... fields) {
        assert domain: "domain is required"
        assert type: "type is required"
        assert fields: "fields is required"
        resmiService.addSearchableFields(new SearchResource(domain, type, fields.collect().toSet()))
    }

    @Description("Full text search fields in a type.")
    def searchableRelationFields(String domain, String type, String relation, String... fields) {
        assert domain: "domain is required"
        assert type: "type is required"
        assert relation: "relation is required"
        assert fields: "fields is required"
        resmiService.addSearchableFields(new SearchResource(domain, type, relation, fields.collect().toSet()))
    }

    @Description("Raw creation of an index with settings.")
    def createIndex(String domain, String name, String settings) {
        assert domain: "domain is required"
        assert name: "name is required"
        assert settings: "settings is required"
        resmiSearch.createIndex(domain, name, settings)
    }

    @Description("Defines an resmi index with settings if not exists.")
    def upsertIndex(String domain, String name, String settings) {
        assert domain: "domain is required"
        assert name: "name is required"
        assert settings: "settings is required"
        resmiSearch.upsertResmiIndex(domain, name, settings)
    }

    @Description("Defines a full text search mapping for a type, creating its resmi index if not exists.")
    def setMapping(String domain, String type, String mapping) {
        assert domain: "domain is required"
        assert type: "type is required"
        assert mapping: "mapping is required"
        def uri = new ResourceUri(domain, type)
        resmiSearch.upsertResmiIndex(uri)
        resmiSearch.setupMapping(uri, mapping)
    }

    @Description("Defines a full text search mapping for a relation, creating its resmi index if not exists.")
    def setRelationMapping(String domain, String type, String relation, String mapping) {
        assert domain: "domain is required"
        assert type: "type is required"
        assert relation: "relation is required"
        assert mapping: "mapping is required"
        def uri = new ResourceUri(domain, type, null, relation)
        resmiSearch.upsertResmiIndex(uri)
        resmiSearch.setupMapping(uri, mapping)
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
