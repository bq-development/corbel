package io.corbel.resources.rem.health;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.node.DiscoveryNode;

import com.codahale.metrics.health.HealthCheck;

import java.util.List;

public class ElasticSearchHealthCheck extends HealthCheck {

    private final TransportClient elasticsearchClient;

    public ElasticSearchHealthCheck(Client elasticsearchClient) {
        this.elasticsearchClient = (TransportClient) elasticsearchClient;
    }

    public ElasticSearchHealthCheck() {
        this.elasticsearchClient = null;
    }

    @Override
    protected Result check() throws Exception {
        if(elasticsearchClient == null) {
            return Result.healthy("ES disabled, you can activate it via properties");
        }
        if (elasticsearchClient.connectedNodes().isEmpty()) {
            return Result.unhealthy("No nodes available, verify ES is running correctly!");
        }
        return Result.healthy();
    }

}
