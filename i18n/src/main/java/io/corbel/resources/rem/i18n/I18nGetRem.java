package io.corbel.resources.rem.i18n;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.core.Response;

import org.eclipse.jetty.http.HttpStatus;
import org.springframework.http.HttpMethod;

import io.corbel.resources.rem.i18n.api.I18nErrorResponseFactory;
import io.corbel.resources.rem.i18n.model.I18n;
import io.corbel.resources.rem.request.CollectionParameters;
import io.corbel.resources.rem.request.RequestParameters;
import io.corbel.resources.rem.request.ResourceId;
import io.corbel.resources.rem.request.ResourceParameters;
import io.corbel.lib.queries.QueryNodeImpl;
import io.corbel.lib.queries.StringQueryLiteral;
import io.corbel.lib.queries.request.QueryOperator;
import io.corbel.lib.queries.request.ResourceQuery;
import com.google.gson.JsonArray;


public class I18nGetRem extends I18nBaseRem {

    @Override
    public Response collection(String type, RequestParameters<CollectionParameters> parameters, URI uri, Optional<I18n> entity) {
        return Optional.ofNullable(getLanguage(parameters.getHeaders())).map(languageHeader -> {
            List<ResourceQuery> baseQueries = parameters.getApiParameters().getQueries().orElse(Arrays.asList(new ResourceQuery()));
            for (String language : getProcessedLanguage(languageHeader)) {
                List<ResourceQuery> resourceQueries = new ArrayList<>();
                baseQueries.forEach(baseQuery -> {
                    ResourceQuery resourceQuery = new ResourceQuery();
                    baseQuery.forEach(resourceQuery::addQueryNode);
                    addQueryLanguage(language, resourceQuery);
                    resourceQueries.add(resourceQuery);
                });

                parameters.getApiParameters().setQueries(Optional.of(resourceQueries));

                Response response = getJsonRem(type, HttpMethod.GET).collection(type, parameters, uri, entity);

                if (response.getStatus() == HttpStatus.OK_200) {
                    JsonArray dict = (JsonArray) response.getEntity();
                    if (dict.size() > 0) {
                        return response;
                    }
                }
            }
            return I18nErrorResponseFactory.getInstance().notFound();
        }).orElse(I18nErrorResponseFactory.getInstance().errorNotLanguageHeader());
    }

    @Override
    public Response resource(String type, ResourceId id, RequestParameters<ResourceParameters> parameters, Optional<I18n> entity) {
        return Optional.ofNullable(getLanguage(parameters.getHeaders())).map(languageHeader -> {
            Response response;
            ResourceId newId;
            for (String language : getProcessedLanguage(languageHeader)) {
                newId = new ResourceId(language + ID_SEPARATOR + id.getId());
                response = getJsonRem(type, HttpMethod.GET).resource(type, newId, parameters, entity);
                if (response.getStatus() == HttpStatus.OK_200) {
                    return response;
                }
            }
            return I18nErrorResponseFactory.getInstance().notFound();
        }).orElse(I18nErrorResponseFactory.getInstance().errorNotLanguageHeader());
    }

    private void addQueryLanguage(String language, ResourceQuery resourceQuery) {
        resourceQuery.addQueryNode(new QueryNodeImpl(QueryOperator.$LIKE, "id", new StringQueryLiteral(language + ":")));
    }

    @Override
    public Class<I18n> getType() {
        return I18n.class;
    }

}
