package com.k8s.network.controller.backend.model.networking;

import lombok.Data;

import java.util.Map;

@Data
public class NetworkPolicyPeer {
    private IPBlock ipBlock;
    private String namespaceName;
    private Map<String, String> podSelectorLabels;

}
