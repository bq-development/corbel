package io.corbel.evci.eworker;

import java.util.List;

/**
 * @author Cristian del Cerro
 */
public interface EworkerArtifactIdRegistry {

    /**
     * Adds eworker artifact Id
     *
     * @param artifactId Artifact Id to be added
     */
    void addEworkerArtifactId(String artifactId);

    /**
     *
     * @return List of eworker info
     */
    List<String> getEworkerArtifactId();
}
