package io.corbel.resources.rem.service;

import io.corbel.resources.rem.Rem;
import io.corbel.resources.rem.model.ManagedCollection;

import java.net.URI;
import java.util.List;

import javax.ws.rs.core.Response;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.HttpMethod;

public interface AclConfigurationService {

    void setRemsAndMethods(List<Pair<Rem, HttpMethod>> remsAndMethods);

    Response updateConfiguration(String id, ManagedCollection managedCollection);

    void addAclConfiguration(String collection);

    void removeAclConfiguration(String id, String collection);

    void refreshRegistry();

    void setRemService(RemService remService);

    Response createConfiguration(URI uri, ManagedCollection managedCollection);

    Response getConfiguration(String id);

    void setResourcesWithDefaultPermission(String collectionName, String domain, String defaultPermission);

    Response getConfigurations(String domain);

    Response getConfiguration(String id, String domain);
}
