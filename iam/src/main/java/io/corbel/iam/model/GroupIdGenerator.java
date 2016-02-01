package io.corbel.iam.model;

import com.google.common.base.Joiner;

import io.corbel.lib.mongo.IdGenerator;
import io.corbel.lib.ws.digest.Digester;

public class GroupIdGenerator implements IdGenerator<Group> {

    private static final char SEPARATOR = ':';

    private final Digester digester;

    public GroupIdGenerator(Digester digester) {
        this.digester = digester;
    }

    @Override
    public String generateId(Group entity) {
        return digester.digest(Joiner.on(SEPARATOR).join(entity.getDomain(), entity.getName()));
    }

}
