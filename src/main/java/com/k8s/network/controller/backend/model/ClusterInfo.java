package com.k8s.network.controller.backend.model;

import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.ServiceList;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class ClusterInfo {
    private final String masterUrl;
    private final Map<String, Namespace> namespaces = new HashMap<>();
    public ClusterInfo(String masterUrl) {
        this.masterUrl = masterUrl;
    }

    public void addNamespace(String name, PodList podList, ServiceList serviceList) {
        Namespace namespace = new Namespace(this, podList, serviceList);
        namespaces.put(name, namespace);
    }
}
