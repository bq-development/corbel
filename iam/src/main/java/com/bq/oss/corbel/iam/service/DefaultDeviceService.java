package com.bq.oss.corbel.iam.service;

import java.util.List;

import com.bq.oss.corbel.iam.model.Device;
import com.bq.oss.corbel.iam.model.User;
import com.bq.oss.corbel.iam.repository.DeviceRepository;
import io.corbel.lib.mongo.IdGenerator;

/**
 * @author Francisco Sanchez
 */
public class DefaultDeviceService implements DeviceService {

    private final DeviceRepository deviceRepository;
    private final IdGenerator<Device> deviceIdGenerator;

    public DefaultDeviceService(DeviceRepository deviceRepository, IdGenerator<Device> deviceIdGenerator) {
        this.deviceRepository = deviceRepository;
        this.deviceIdGenerator = deviceIdGenerator;
    }

    @Override
    public Device get(String deviceId) {
        return deviceRepository.findOne(deviceId);
    }

    @Override
    public Device getByIdAndUserId(String deviceId, String userId) {
        return deviceRepository.findByIdAndUserId(deviceId, userId);
    }

    @Override
    public List<Device> getByUserId(String userId) {
        return deviceRepository.findByUserId(userId);
    }

    @Override
    public Device update(Device device) {
        return deviceRepository.save(device);
    }

    @Override
    public void deleteByIdAndUserId(String deviceId, String userId) {
        deviceRepository.deleteByIdAndUserId(deviceId, userId);
    }

    @Override
    public List<Device> deleteByUserId(User user) {
        return deviceRepository.deleteByUserId(user.getId());
    }
}
