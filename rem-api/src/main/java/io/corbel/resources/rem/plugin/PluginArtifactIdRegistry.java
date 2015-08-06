package io.corbel.resources.rem.plugin;

import java.util.List;

public interface PluginArtifactIdRegistry {

    void addPluginArtifactId(String artifactId);

    List<String> getPluginsArtifactId();
}
