package io.corbel.resources.cli.dsl

import io.corbel.resources.model.RelationSchema
import io.corbel.resources.rem.Rem
import io.corbel.resources.rem.service.RemService
import io.corbel.resources.repository.RelationSchemaRepository
import io.corbel.lib.cli.console.Description
import io.corbel.lib.cli.console.Shell
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired

/**
 * @author Rubén Carrasco
 *
 */
@Shell("resources")
class ResourcesShell {

    static final Logger LOG = LoggerFactory.getLogger(ResourcesShell.class)

    RelationSchemaRepository relationSchemaRepository
    RemService remService

    @Autowired
    ResourcesShell(RelationSchemaRepository relationSchemaRepository, RemService remService) {
        this.relationSchemaRepository = relationSchemaRepository
        this.remService = remService
    }

    @Description('Adds a relation schema')
    def createRelationSchema(String type, Map relations) {
        assert type: 'type is required'
        assert relations: 'relations is required'

        RelationSchema relationSchema = new RelationSchema()
        relationSchema.type = type
        relationSchema.relations = relations
        relationSchemaRepository.save(relationSchema)
    }

    @Description('Print the description of all installed plugins')
    void printRemPluginDescriptions() {
        def remDescriptions = remService.getRegisteredRemDescriptions()

        remDescriptions.forEach { remDescription ->
            println(remDescription.toString())
        }
    }

    @Description('Get a Rem plugin by name')
    Rem getRem(String remName) {
        Rem rem = remService.getRem(remName)
        if (rem == null) LOG.warn("Rem plugin ${remName} not found.")
        return rem
    }
}