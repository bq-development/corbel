package io.corbel.resources.rem.service;

import io.corbel.resources.rem.Rem;
import io.corbel.resources.rem.model.ManagedCollection;
import io.corbel.resources.rem.request.ResourceId;

import java.util.List;

import javax.ws.rs.core.Response;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.HttpMethod;

public interface AclConfigurationService {

    void setRemsAndMethods(List<Pair<Rem, HttpMethod>> remsAndMethods);

    Response updateConfiguration(ResourceId id, ManagedCollection managedCollection);

    void addAclConfiguration(String collection);

    void removeAclConfiguration(String collection);

    void refreshRegistry();

    void setRemService(RemService remService);
}
