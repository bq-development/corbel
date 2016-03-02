package io.corbel.iam.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import io.corbel.iam.model.UserToken;

/**
 * @author Cristian del Cerro
 */
public interface UserTokenRepository extends CrudRepository<UserToken, String> {

    UserToken findByToken(String id);
    List<UserToken> findByUserId(String userId);
    List<UserToken> findByDeviceId(String deviceId);
}
