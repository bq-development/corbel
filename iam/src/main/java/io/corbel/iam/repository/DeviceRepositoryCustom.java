package io.corbel.iam.repository;

import java.util.Date;

/**
 * @author Francisco Sanchez
 */
public interface DeviceRepositoryCustom {

    void updateLastConnectionIfExist(String deviceId, Date lastConnection);

}
