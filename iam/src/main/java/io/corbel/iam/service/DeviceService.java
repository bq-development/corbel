package io.corbel.iam.service;

import io.corbel.iam.model.Device;
import io.corbel.iam.model.User;
import io.corbel.lib.queries.jaxrs.QueryParameters;

import java.util.List;

/**
 * @author Francisco Sanchez
 */
public interface DeviceService {

    Device get(String deviceId);

    Device getByUidAndUserId(String deviceId, String userId, String domain);

    List<Device> getByUserId(String userId, QueryParameters queryParameters);

    Device update(Device device);

    void deviceConnect(String domain, String userId, String uid);

    void deleteByUidAndUserId(String deviceId, String userId, String domainId);

    List<Device> deleteByUserId(User user);

    Device update(Device device, boolean connected);
}
