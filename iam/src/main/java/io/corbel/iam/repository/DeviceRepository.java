package io.corbel.iam.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import io.corbel.iam.model.Device;
import io.corbel.lib.mongo.repository.PartialUpdateRepository;

/**
 * @author Francisco Sanchez
 */
public interface DeviceRepository extends CrudRepository<Device, String>, PartialUpdateRepository<Device, String> {

    List<Device> findByUserId(String userId);

    Device findById(String id);

    Long deleteById(String id);

    List<Device> deleteByUserId(String id);

}
