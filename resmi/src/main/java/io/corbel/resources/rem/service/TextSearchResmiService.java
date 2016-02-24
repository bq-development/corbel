package io.corbel.resources.rem.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import io.corbel.lib.queries.request.Search;
import io.corbel.resources.rem.dao.ResmiDao;
import io.corbel.resources.rem.model.ResourceUri;
import io.corbel.resources.rem.request.CollectionParameters;
import io.corbel.resources.rem.request.RelationParameters;
import io.corbel.resources.rem.resmi.exception.InvalidApiParamException;

import java.time.Clock;
import java.util.List;
import java.util.Optional;

public class TextSearchResmiService extends DefaultResmiService {

    public TextSearchResmiService(ResmiDao resmiDao, Clock clock) {
        super(resmiDao, clock);
    }

    @Override
    public JsonArray findCollection(ResourceUri uri, Optional<CollectionParameters> apiParameters) throws BadConfigurationException, InvalidApiParamException {
        return resmiDao.findCollection(uri, apiParameters.flatMap(CollectionParameters::getQueries), apiParameters.flatMap(CollectionParameters::getSearch).flatMap(Search::getText), apiParameters.map(CollectionParameters::getPagination), apiParameters.flatMap(CollectionParameters::getSort));
    }

    @Override
    public JsonArray findCollectionDistinct(ResourceUri uri, Optional<CollectionParameters> apiParameters, List<String> fields, boolean first) throws BadConfigurationException, InvalidApiParamException {
        return resmiDao.findCollectionWithGroup(uri, apiParameters.flatMap(CollectionParameters::getQueries), apiParameters.flatMap(CollectionParameters::getSearch).flatMap(Search::getText), apiParameters.map(CollectionParameters::getPagination), apiParameters.flatMap(CollectionParameters::getSort), fields,
                first);
    }

    @Override
    public JsonElement findRelation(ResourceUri uri, Optional<RelationParameters> apiParameters) throws BadConfigurationException, InvalidApiParamException {
        return resmiDao.findRelation(uri, apiParameters.flatMap(RelationParameters::getQueries), apiParameters.flatMap(CollectionParameters::getSearch).flatMap(Search::getText), apiParameters.map(RelationParameters::getPagination), apiParameters.flatMap(RelationParameters::getSort));
    }

    @Override
    public JsonArray findRelationDistinct(ResourceUri uri, Optional<RelationParameters> apiParameters, List<String> fields, boolean first)
            throws BadConfigurationException, InvalidApiParamException {
        return resmiDao.findRelationWithGroup(uri, apiParameters.flatMap(RelationParameters::getQueries), apiParameters.flatMap(CollectionParameters::getSearch).flatMap(Search::getText), apiParameters.map(RelationParameters::getPagination), apiParameters.flatMap(RelationParameters::getSort), fields, first);
    }
}
