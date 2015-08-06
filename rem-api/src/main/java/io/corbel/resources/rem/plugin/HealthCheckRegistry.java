package io.corbel.resources.rem.plugin;

import java.util.Map;

import com.codahale.metrics.health.HealthCheck;

public interface HealthCheckRegistry {

    void addHealthCheck(String name, HealthCheck healthCheck);

    Map<String, HealthCheck> getHealthChecks();
}
