package io.corbel.iam.repository;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import io.corbel.iam.model.Device;

/**
 * @author Francisco Sanchez
 */
public class DeviceRepositoryImpl implements DeviceRepositoryCustom {

    private final MongoOperations mongo;

    @Autowired
    public DeviceRepositoryImpl(MongoOperations mongo) {
        this.mongo = mongo;
    }

    public void updateLastConnectionIfExist(String deviceId, Date lastConnection) {
        Query query = Query.query(Criteria.where("_id").is(deviceId));
        Update update = new Update();
        update.set(Device.LAST_CONNECTION_FIELD, lastConnection);
        mongo.findAndModify(query, update, FindAndModifyOptions.options().upsert(false), Device.class);
    }

}
