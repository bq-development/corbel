package com.bq.oss.corbel.iam.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.bq.oss.corbel.iam.model.Device;
import io.corbel.lib.mongo.repository.PartialUpdateRepository;

/**
 * @author Francisco Sanchez
 */
public interface DeviceRepository extends CrudRepository<Device, String>, PartialUpdateRepository<Device, String> {

    List<Device> findByUserId(String userId);

    Device findByIdAndUserId(String id, String userId);

    Long deleteByIdAndUserId(String deviceId, String userId);

    List<Device> deleteByUserId(String id);

}
