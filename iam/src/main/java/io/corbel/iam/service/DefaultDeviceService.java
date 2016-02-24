package io.corbel.iam.service;

import java.time.Clock;
import java.util.Date;
import java.util.List;

import io.corbel.iam.model.Device;
import io.corbel.iam.model.User;
import io.corbel.iam.repository.DeviceRepository;
import io.corbel.iam.utils.UserDomainIdGenerator;
import io.corbel.lib.mongo.IdGenerator;

/**
 * @author Francisco Sanchez
 */
public class DefaultDeviceService implements DeviceService {

    private final DeviceRepository deviceRepository;
    private final IdGenerator<Device> deviceIdGenerator;
    private final EventsService eventsService;
    private final Clock clock;

    public DefaultDeviceService(DeviceRepository deviceRepository, IdGenerator<Device> deviceIdGenerator, EventsService eventsService,
            Clock clock) {
        this.deviceRepository = deviceRepository;
        this.deviceIdGenerator = deviceIdGenerator;
        this.eventsService = eventsService;
        this.clock = clock;
    }

    @Override
    public Device get(String deviceId) {
        return deviceRepository.findOne(deviceId);
    }

    @Override
    public Device getByUidAndUserId(String deviceUid, String userId, String domain) {
        String deviceId = UserDomainIdGenerator.generateDeviceId(domain, userId, deviceUid);
        return deviceRepository.findById(deviceId);
    }

    @Override
    public List<Device> getByUserId(String userId) {
        return deviceRepository.findByUserId(userId);
    }

    @Override
    public Device update(Device device) {
        device.setId(deviceIdGenerator.generateId(device));
        device.setCreatedAt(null);
        device.setUpdatedAt(Date.from(clock.instant()));
        boolean isPartialUpdate = deviceRepository.upsert(device.getId(), device);
        if (isPartialUpdate) {
            eventsService.sendDeviceUpdateEvent(device);
        } else {
            device.setCreatedAt(device.getUpdatedAt());
            deviceRepository.upsert(device.getId(), new Device().setCreatedAt(device.getUpdatedAt()));
            eventsService.sendDeviceCreateEvent(device);
        }
        return device;
    }

    @Override
    public void deleteByUidAndUserId(String deviceUid, String userId, String domainId) {
        String deviceId = UserDomainIdGenerator.generateDeviceId(domainId, userId, deviceUid);
        long result = deviceRepository.deleteById(deviceId);
        if ( result > 0 ) {
            eventsService.sendDeviceDeleteEvent(deviceId, userId, domainId);
        }
    }

    @Override
    public List<Device> deleteByUserId(User user) {
        return deviceRepository.deleteByUserId(user.getId());
    }
}
