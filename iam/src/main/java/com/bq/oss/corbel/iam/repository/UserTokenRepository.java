package com.bq.oss.corbel.iam.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.bq.oss.corbel.iam.model.UserToken;

/**
 * @author Cristian del Cerro
 */
public interface UserTokenRepository extends CrudRepository<UserToken, String> {

    List<UserToken> findByUserId(String userId);
}
