package com.k8s.network.controller.backend.service;

import com.k8s.network.controller.backend.model.ClusterInfo;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.ServiceList;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class LocalKubernetesClusterService {
    private final DefaultKubernetesClient client;

    public LocalKubernetesClusterService() {
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

    public ClusterInfo getClusterInfo() {
        return getClusterInfo(getAllNamespaceNames());
    }

    public ClusterInfo getClusterInfo(Set<String> namespaces) {
        ClusterInfo clusterInfo = new ClusterInfo(client.getMasterUrl().toString());
        for(String name : namespaces) {
            PodList podList = client.inNamespace(name).pods().list();
            ServiceList serviceList = client.inNamespace(name).services().list();
            clusterInfo.addNamespace(name, podList, serviceList);
        }
        return clusterInfo;
    }
}
