package io.corbel.iam.model;

import io.corbel.lib.mongo.IdGenerator;
import io.corbel.lib.ws.digest.Digester;
import com.google.common.base.Joiner;

/**
 * @author Alexander De Leon
 * 
 */
public class DeviceIdGenerator implements IdGenerator<Device> {

    private static final String SEPARATOR = ".";
    private final Digester digester;

    public DeviceIdGenerator(Digester digester) {
        this.digester = digester;
    }

    @Override
    public String generateId(Device device) {
        return digester.digest(Joiner.on(SEPARATOR).join(device.getDomain(), device.getUid()));
    }

}
