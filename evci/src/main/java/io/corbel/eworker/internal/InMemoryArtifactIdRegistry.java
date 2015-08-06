package io.corbel.eworker.internal;

import io.corbel.evci.eworker.EworkerArtifactIdRegistry;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Cristian del Cerro
 */
public class InMemoryArtifactIdRegistry implements EworkerArtifactIdRegistry {

    private final List<String> eworkersArtifactId = new ArrayList<>();

    @Override
    public void addEworkerArtifactId(String artifactId) {
        eworkersArtifactId.add(artifactId);
    }

    @Override
    public List<String> getEworkerArtifactId() {
        return eworkersArtifactId;
    }
}
