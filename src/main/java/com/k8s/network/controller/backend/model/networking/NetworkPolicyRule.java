package com.k8s.network.controller.backend.model.networking;

import lombok.Data;

import java.util.List;

@Data
public class NetworkPolicyRule {
    private List<NetworkPolicyPort> ports;
    private NetworkPolicyPeer selector;
}
