package com.k8s.network.controller.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceList;
import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
public class Namespace {
    @JsonIgnore
    private final ClusterInfo clusterInfo;
    private final List<Pod> pods;
    private final List<Service> services;

    public Namespace(ClusterInfo clusterInfo, PodList podList, ServiceList serviceList) {
        this.clusterInfo = clusterInfo;
        this.pods = Collections.unmodifiableList(podList.getItems());
        this.services = Collections.unmodifiableList(serviceList.getItems());
    }

}
