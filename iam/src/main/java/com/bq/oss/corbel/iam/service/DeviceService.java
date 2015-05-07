package com.bq.oss.corbel.iam.service;

import java.util.List;

import com.bq.oss.corbel.iam.model.Device;
import com.bq.oss.corbel.iam.model.User;

/**
 * @author Francisco Sanchez
 */
public interface DeviceService {

    Device get(String deviceId);

    Device getByIdAndUserId(String deviceId, String userId);

    List<Device> getByUserId(String userId);

    Device update(Device deviceId);

    void deleteByIdAndUserId(String deviceId, String userId);

    List<Device> deleteByUserId(User user);
}
