package io.corbel.resources.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.corbel.resources.model.PluginInfo;
import io.corbel.resources.rem.plugin.PluginArtifactIdRegistry;

/**
 * @author Cristian del Cerro
 */

@Resource @Path("/plugins") public class PluginInfoResource {

    private static final Logger LOG = LoggerFactory.getLogger(PluginInfoResource.class);

    private final List<String> propertyFiles = new ArrayList<>();
    private final List<PluginInfo> pluginsInfo;

    public PluginInfoResource(PluginArtifactIdRegistry pluginArtifactIdRegistry) {
        pluginArtifactIdRegistry.getPluginsArtifactId().stream().forEach(artifactId -> {
            propertyFiles.add("/META-INF/" + artifactId + "-plugin-build.properties");
        });

        pluginsInfo = getPluginsInfo();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPluginInfo() {
        return Response.ok().entity(pluginsInfo).build();
    }

    private List<PluginInfo> getPluginsInfo() {
        List<Properties> buildMetadataProperties = loadBuildMetadataProperties();
        List<PluginInfo> pluginsInfo = new ArrayList<>();

        buildMetadataProperties.stream().forEach(properties -> {
            pluginsInfo.add(new PluginInfo(properties.getProperty("build.artifactId"), properties.getProperty("build.version")));
        });

        return pluginsInfo;
    }

    private List<Properties> loadBuildMetadataProperties() {
        List<Properties> buildMetadataProperties = new ArrayList<>();

        for (String propertyFile : propertyFiles) {
            try(InputStream buildPropertiesStream = PluginInfoResource.class.getResourceAsStream(propertyFile)) {
                if (buildPropertiesStream != null) {
                    Properties prop = new Properties();
                    prop.load(buildPropertiesStream);
                    buildMetadataProperties.add(prop);
                }
            } catch (IOException e) {
                LOG.warn("Problem loading metadata file: " + propertyFile, e);
            }
        }

        return buildMetadataProperties;
    }

}
