package io.corbel.rem.internal;

import java.util.ArrayList;
import java.util.List;

import io.corbel.resources.rem.plugin.PluginArtifactIdRegistry;

/**
 * @author Cristian del Cerro
 */
public class InMemoryPluginArtifactIdRegistry implements PluginArtifactIdRegistry {

    private final List<String> pluginsArtifactId = new ArrayList<>();

    @Override
    public void addPluginArtifactId(String artifactId) {
        pluginsArtifactId.add(artifactId);
    }

    @Override
    public List<String> getPluginsArtifactId() {
        return pluginsArtifactId;
    }
}
