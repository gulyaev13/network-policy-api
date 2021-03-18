package com.k8s.network.controller.backend.model.networking;

import lombok.Data;

import java.util.Map;

@Data
public class NetworkPolicyPattern {
    private String issue;
    private String description;

    private String name;
    private String namespace;

    private Map<String, String> podSelectorLabels;
    private NetworkPolicyRule egress;
    private NetworkPolicyRule ingress;
}
