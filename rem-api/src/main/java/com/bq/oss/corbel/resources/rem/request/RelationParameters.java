package com.bq.oss.corbel.resources.rem.request;

import java.util.Optional;

/**
 * @author Alexander De Leon
 * 
 */
public interface RelationParameters extends CollectionParameters {

    /**
     * URI of the resource in the relation's predicates (e.g. music:Track/555 in music:Playlist/123/music:tracks;r=music:Track/555)
     * 
     * @return Optional string of the predicate resource uri
     */
    Optional<String> getPredicateResource();
}
