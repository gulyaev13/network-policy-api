package com.k8s.network.controller.backend.client;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class KubernetesClientWrapper {
    private final DefaultKubernetesClient client;

    public KubernetesClientWrapper() {
        client = new DefaultKubernetesClient();
    }

    public DefaultKubernetesClient getClient() {
        return client;
    }

    public Set<String> getAllNamespaceNames() {
        return client.namespaces()
                .list()
                .getItems()
                .stream()
                .map(io.fabric8.kubernetes.api.model.Namespace::getMetadata)
                .map(ObjectMeta::getName)
                .collect(Collectors.toSet());
    }
}
