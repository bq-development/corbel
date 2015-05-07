package com.bq.oss.corbel.iam.repository;

import com.bq.oss.corbel.iam.model.Domain;
import com.bq.oss.lib.mongo.repository.PartialUpdateRepository;
import com.bq.oss.lib.queries.mongo.repository.GenericFindRepository;

/**
 * @author Alberto J. Rubio
 */
public interface DomainRepository extends PartialUpdateRepository<Domain, String>, GenericFindRepository<Domain, String>,
        DomainRepositoryCustom, HasScopesRepository<String> {

    String FIELD_DEFAULT_SCOPES = "defaultScopes";

}
