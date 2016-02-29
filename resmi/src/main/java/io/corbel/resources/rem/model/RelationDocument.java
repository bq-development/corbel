package io.corbel.resources.rem.model;

public class RelationDocument extends GenericDocument {

    private String _dst_id;

    public String get_dst_id() {
        return _dst_id;
    }

    public RelationDocument set_dst_id(String _dst_id) {
        this._dst_id = _dst_id;
        return this;
    }
}
