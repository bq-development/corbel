package io.corbel.evci.api;

import io.corbel.evci.eworker.EworkerArtifactIdRegistry;
import io.corbel.evci.model.EworkerInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author Cristian del Cerro
 */

@Resource
@Path("/plugins")
public class EworkerInfoResource {

    private static final Logger LOG = LoggerFactory.getLogger(EworkerInfoResource.class);

    private final List<String> propertyFiles = new ArrayList<>();
    private final List<EworkerInfo> eworkersInfo;

    public EworkerInfoResource(EworkerArtifactIdRegistry eworkerArtifactIdRegistry) {
        eworkerArtifactIdRegistry.getEworkerArtifactId().stream().forEach(artifactId -> {
            propertyFiles.add("/META-INF/"+artifactId+"-eworker-build.properties");
        });

        eworkersInfo = getPluginsInfo();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPluginInfo() {
        return Response.ok().entity(eworkersInfo).build();
    }

    private List<EworkerInfo> getPluginsInfo()  {
        List<Properties> buildMetadataProperties = loadBuildMetadataProperties();
        List<EworkerInfo> pluginsInfo = new ArrayList<>();

        buildMetadataProperties.stream().forEach(properties -> {
            pluginsInfo.add(new EworkerInfo(properties.getProperty("build.artifactId"),
                    properties.getProperty("build.version")));
        });

        return pluginsInfo;
    }

    private List<Properties> loadBuildMetadataProperties() {
        List<Properties> buildMetadataProperties = new ArrayList<>();

        for (String propertyFile : propertyFiles) {
            try(InputStream buildPropertiesStream = EworkerInfoResource.class.getResourceAsStream(propertyFile)) {
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
