package io.corbel.iam.repository;

import io.corbel.iam.model.Domain;
import io.corbel.lib.mongo.repository.PartialUpdateRepository;
import io.corbel.lib.queries.mongo.repository.GenericFindRepository;

/**
 * @author Alberto J. Rubio
 */
public interface DomainRepository extends PartialUpdateRepository<Domain, String>, GenericFindRepository<Domain, String>,
        DomainRepositoryCustom, HasScopesRepository<String> {

    String FIELD_DEFAULT_SCOPES = "defaultScopes";

    String FIELD_PUBLIC_SCOPES = "publicScopes";

}
