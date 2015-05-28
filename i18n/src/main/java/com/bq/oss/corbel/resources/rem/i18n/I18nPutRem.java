package com.bq.oss.corbel.resources.rem.i18n;

import java.util.Optional;

import javax.ws.rs.core.Response;

import org.springframework.http.HttpMethod;

import com.bq.oss.corbel.resources.rem.i18n.api.I18nErrorResponseFactory;
import com.bq.oss.corbel.resources.rem.i18n.model.I18n;
import com.bq.oss.corbel.resources.rem.request.RequestParameters;
import com.bq.oss.corbel.resources.rem.request.ResourceId;
import com.bq.oss.corbel.resources.rem.request.ResourceParameters;
import com.google.common.base.Strings;
import com.google.gson.Gson;

public class I18nPutRem extends I18nBaseRem {

    private final Gson gson;

    public I18nPutRem(Gson gson) {
        this.gson = gson;
    }

    @Override
    public Response resource(String type, ResourceId id, RequestParameters<ResourceParameters> parameters, Optional<I18n> optionalEntity) {
        return optionalEntity.map(entity -> {
            if (Strings.isNullOrEmpty(entity.getValue()) || Strings.isNullOrEmpty(entity.getLang())) {
                return null;
            }
            entity.setKey(id.getId());
            id.setId(entity.getLang() + ID_SEPARATOR + id.getId());
            entity.setId(id.getId());
            return gson.toJsonTree(entity).getAsJsonObject();
        }).map(entityJson -> {
            return getJsonRem(type, HttpMethod.PUT).resource(type, id, parameters, Optional.of(entityJson));
        }).orElse(I18nErrorResponseFactory.getInstance().invalidEntity("I18n requires key and lang values"));
    }

    @Override
    public Class<I18n> getType() {
        return I18n.class;
    }

}
