package io.corbel.resources.rem.i18n;

import java.util.Optional;

import javax.ws.rs.core.Response;

import org.springframework.http.HttpMethod;

import io.corbel.resources.rem.i18n.api.I18nErrorResponseFactory;
import io.corbel.resources.rem.i18n.model.I18n;
import io.corbel.resources.rem.request.RequestParameters;
import io.corbel.resources.rem.request.ResourceId;
import io.corbel.resources.rem.request.ResourceParameters;

public class I18nDeleteRem extends I18nBaseRem {

    @Override
    public Response resource(String type, ResourceId id, RequestParameters<ResourceParameters> parameters, Optional<I18n> optionalEntity) {
        return Optional.ofNullable(getLanguage(parameters.getHeaders())).map(language -> {
            id.setId(language + ID_SEPARATOR + id.getId());
            return getJsonRem(type, HttpMethod.DELETE).resource(type, id, parameters, Optional.empty());
        }).orElse(I18nErrorResponseFactory.getInstance().errorNotLanguageHeader());

    }

    @Override
    public Class<I18n> getType() {
        return I18n.class;
    }

}
