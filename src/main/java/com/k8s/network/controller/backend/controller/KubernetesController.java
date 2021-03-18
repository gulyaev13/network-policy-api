package com.k8s.network.controller.backend.controller;

import com.k8s.network.controller.backend.model.ClusterInfo;
import com.k8s.network.controller.backend.service.LocalKubernetesClusterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;


@RestController("cluster/info")
public class KubernetesController {

    @Autowired
    private LocalKubernetesClusterService localKubernetes;

    @GetMapping("cluster/info")
    public ClusterInfo clusterInfo(@RequestParam(name = "namespaces", required = false) Set<String> namespaces) {
        if(namespaces == null || namespaces.isEmpty()){
            return localKubernetes.getClusterInfo();
        }
        return localKubernetes.getClusterInfo(namespaces);
    }
}