package io.corbel.iam.service;

import java.util.List;

import io.corbel.iam.model.Device;
import io.corbel.iam.model.User;

/**
 * @author Francisco Sanchez
 */
public interface DeviceService {

    Device get(String deviceId);

    Device getByUidAndUserId(String deviceId, String userId, String domain);

    List<Device> getByUserId(String userId);

    Device update(Device deviceId);

    void deleteByUidAndUserId(String deviceId, String userId, String domainId);

    List<Device> deleteByUserId(User user);
}
