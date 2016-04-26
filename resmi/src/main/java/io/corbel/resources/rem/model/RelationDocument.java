package io.corbel.resources.rem.model;

import org.springframework.data.mongodb.core.mapping.Field;

public class RelationDocument extends GenericDocument {

    @Field("_dst_id")
    private String dstId;

    public String getDstId() {
        return dstId;
    }

    public RelationDocument setDstId(String dstId) {
        this.dstId = dstId;
        return this;
    }
}
