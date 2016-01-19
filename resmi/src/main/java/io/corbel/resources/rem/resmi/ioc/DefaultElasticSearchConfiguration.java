package io.corbel.resources.rem.resmi.ioc;

import java.util.Optional;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.transport.TransportAddress;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * @author Francisco Sanchez
 */
@Configuration public class DefaultElasticSearchConfiguration {

    @Value("${resmi.elasticsearch.nodes:localhost:9300}") private String nodes;
    @Value("${resmi.elasticsearch.user:null}") private String user;
    @Value("${resmi.elasticsearch.password:null}") private String password;
    @Value("${resmi.elasticsearch.cluster.name:silkroad-integration}") private String clusterName;

    @Lazy
    @Bean
    public Client elasticsearchTemplate() {
        ImmutableSettings.Builder immutableSettingsBuilder = ImmutableSettings.settingsBuilder();
        immutableSettingsBuilder.put("cluster.name", clusterName);
        Optional.ofNullable(user).ifPresent(user -> {
            immutableSettingsBuilder.put("shield.user", user + ":" + password);
        });
        TransportClient transportClient = new TransportClient(immutableSettingsBuilder);
        String[] nodesArray = nodes.split(",");
        TransportAddress[] nodesAddresses = new TransportAddress[nodesArray.length];
        for (int node = 0; node < nodesArray.length; node++) {
            String[] hostAndPort = nodesArray[node].trim().split(":");
            try {
                nodesAddresses[node] = new InetSocketTransportAddress(hostAndPort[0], Integer.parseInt(hostAndPort[1]));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid port for node: " + nodesArray[node], e);
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new IllegalArgumentException("Missing port for node: " + nodesArray[node], e);
            }
        }
        transportClient.addTransportAddresses(nodesAddresses);
        return transportClient;
    }
}
