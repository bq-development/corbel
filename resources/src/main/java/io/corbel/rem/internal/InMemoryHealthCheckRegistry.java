package io.corbel.rem.internal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.corbel.resources.rem.plugin.HealthCheckRegistry;
import com.codahale.metrics.health.HealthCheck;

/**
 * @author Cristian del Cerro
 */
public class InMemoryHealthCheckRegistry implements HealthCheckRegistry {

    private final Map<String, HealthCheck> healthChecks = new ConcurrentHashMap<>();

    @Override
    public void addHealthCheck(String name, HealthCheck healthCheck) {
        healthChecks.put(name, healthCheck);
    }

    @Override
    public Map<String, HealthCheck> getHealthChecks() {
        return healthChecks;
    }
}
