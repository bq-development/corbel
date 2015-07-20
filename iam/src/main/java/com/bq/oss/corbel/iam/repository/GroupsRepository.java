package com.bq.oss.corbel.iam.repository;

import com.bq.oss.corbel.iam.model.Group;
import com.bq.oss.corbel.iam.model.Scope;
import org.springframework.data.repository.CrudRepository;

/**
 * Created by Alberto J. Rubio
 */
public interface GroupsRepository extends CrudRepository<Group, String> {
}
