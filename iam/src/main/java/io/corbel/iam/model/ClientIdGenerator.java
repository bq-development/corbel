package io.corbel.iam.model;

import io.corbel.lib.mongo.IdGenerator;
import io.corbel.lib.ws.digest.Digester;
import com.google.common.base.Joiner;

/**
 * @author Alexander De Leon
 * 
 */
public class ClientIdGenerator implements IdGenerator<Client> {

    private static final String SEPARATOR = ".";
    private final Digester digester;

    public ClientIdGenerator(Digester digester) {
        this.digester = digester;
    }

    @Override
    public String generateId(Client entity) {
        return digester.digest(Joiner.on(SEPARATOR).join(entity.getDomain(), entity.getName()));
    }

}
