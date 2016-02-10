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

    @Override
    protected Result check() throws Exception {
        List<DiscoveryNode> nodes = elasticsearchClient.connectedNodes();
        if (nodes.isEmpty()) {
            return Result.unhealthy("No nodes available. Verify ES is running!");
        } else {
            return Result.healthy();
        }
    }

}
