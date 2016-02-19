package io.corbel.resources.rem.utils;

import java.util.Date;
import java.util.Optional;

import org.bson.types.ObjectId;

import io.corbel.lib.mongo.JsonObjectMongoWriteConverter;
import com.google.gson.JsonElement;
import com.mongodb.DBObject;

public class ResmiJsonObjectMongoWriteConverter extends JsonObjectMongoWriteConverter {

    @Override
    public DBObject convert(JsonElement source) {
        convertIdToString(source);
        DBObject dbo = super.convert(source);
        tryToConvertIdStringToObjectId(dbo);
        tryToConvertExpireToDate(dbo);

        return dbo;
    }

    private void convertIdToString(JsonElement source) {
        if (source.isJsonObject()) {
            JsonElement id = source.getAsJsonObject().get("id");
            if (id != null && id.isJsonPrimitive()) {
                source.getAsJsonObject().addProperty("id", id.getAsString());
            } else {
                source.getAsJsonObject().remove("id");
            }
        }
    }

    private void tryToConvertIdStringToObjectId(DBObject dbo) {
        Optional.ofNullable(dbo.get("_id")).map(Object::toString).map(id -> {
            try {
                return new ObjectId(id);
            } catch (IllegalArgumentException ignored) {
                return null;
            }
        }).ifPresent(objectId -> dbo.put("_id", objectId));
    }

    private void tryToConvertExpireToDate(DBObject dbo) {
        Optional<Object> expireAtOption = Optional.ofNullable(dbo.get("_expireAt"));

        if (!expireAtOption.isPresent()){
            return;
        }
        Date expireAt = expireAtOption.map(expireAtWithoutCast -> {
            try {
                return Long.parseLong(expireAtWithoutCast.toString());
            } catch (NumberFormatException e) {
                return null;
            }
        }).map(Date::new).orElseThrow(IllegalArgumentException::new);

        dbo.put("_expireAt", expireAt);
    }

}
