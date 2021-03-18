package com.k8s.network.controller.backend.converter;

import com.k8s.network.controller.backend.model.networking.NetworkPolicyPattern;
import com.k8s.network.controller.backend.model.networking.NetworkPolicyRule;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.LabelSelector;
import io.fabric8.kubernetes.api.model.LabelSelectorRequirement;
import io.fabric8.kubernetes.api.model.networking.IPBlock;
import io.fabric8.kubernetes.api.model.networking.NetworkPolicy;
import io.fabric8.kubernetes.api.model.networking.NetworkPolicyBuilder;
import io.fabric8.kubernetes.api.model.networking.NetworkPolicyEgressRule;
import io.fabric8.kubernetes.api.model.networking.NetworkPolicyFluent.SpecNested;
import io.fabric8.kubernetes.api.model.networking.NetworkPolicyIngressRule;
import io.fabric8.kubernetes.api.model.networking.NetworkPolicyPeer;
import io.fabric8.kubernetes.api.model.networking.NetworkPolicyPort;
import io.fabric8.kubernetes.api.model.networking.NetworkPolicySpecFluent.IngressNested;
import io.fabric8.kubernetes.api.model.networking.NetworkPolicySpecFluent.EgressNested;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NetworkPolicyConverter {
    private static final String ISSUE_ANNOTATION = "net.policy.k8s.io/issue";
    private static final String DESCRIPTION_ANNOTATION = "net.policy.k8s.io/description";

    public static NetworkPolicy convertPatternToPolicy(NetworkPolicyPattern pattern) {
        NetworkPolicyBuilder builder = new NetworkPolicyBuilder();
        List<String> policyTypes = new ArrayList<>();
        Map<String, String> annotations = new HashMap<>();
        annotations.put(ISSUE_ANNOTATION, pattern.getIssue());
        annotations.put(DESCRIPTION_ANNOTATION, pattern.getDescription());
        builder.withNewMetadata()
                .withName(pattern.getName())
                .withNamespace(pattern.getNamespace())
                .withAnnotations(annotations)
                .endMetadata();

        SpecNested<NetworkPolicyBuilder> spec = builder.withNewSpec();

        // podSelector
        spec.withNewPodSelector()
                .withMatchLabels(pattern.getPodSelectorLabels())
                .endPodSelector();

        // ingress
        if (pattern.getIngress() != null) {
            policyTypes.add("Ingress");
            IngressNested<SpecNested<NetworkPolicyBuilder>> ingress = spec.addNewIngress();
            if (pattern.getIngress().getPorts() != null && !pattern.getIngress().getPorts().isEmpty()) {
                ingress.withPorts(parsePorts(pattern.getIngress().getPorts()));
            }
            if (pattern.getIngress().getSelector() != null) {
                ingress.withFrom(parsePeer(pattern.getIngress().getSelector()));
            }
            ingress.endIngress();
        }

        // egress
        if (pattern.getEgress() != null) {
            policyTypes.add("Egress");
            EgressNested<SpecNested<NetworkPolicyBuilder>> egress = spec.addNewEgress();
            if (pattern.getEgress().getPorts() != null && !pattern.getEgress().getPorts().isEmpty()) {
                egress.withPorts(parsePorts(pattern.getEgress().getPorts()));
            }
            if (pattern.getEgress().getSelector() != null) {
                egress.withTo(parsePeer(pattern.getEgress().getSelector()));
            }
            egress.endEgress();
        }

        // policyTypes
        spec.withPolicyTypes(policyTypes);
        spec.endSpec();

        return builder.build();
    }

    public static NetworkPolicyPattern convertPolicyToPattern(NetworkPolicy policy) {
        NetworkPolicyPattern policyPattern = new NetworkPolicyPattern();
        policyPattern.setName(policy.getMetadata().getName());
        policyPattern.setNamespace(policy.getMetadata().getNamespace());
        policyPattern.setIssue(policy.getMetadata().getAnnotations().get(ISSUE_ANNOTATION));
        policyPattern.setDescription(policy.getMetadata().getAnnotations().get(DESCRIPTION_ANNOTATION));

        policyPattern.setPodSelectorLabels(policy.getSpec().getPodSelector().getMatchLabels());
        if (policy.getSpec().getEgress() != null && !policy.getSpec().getEgress().isEmpty()) {
            NetworkPolicyEgressRule egressRule = policy.getSpec().getEgress().get(0);
            NetworkPolicyRule egress = new NetworkPolicyRule();
            if (!egressRule.getPorts().isEmpty()) {
                egress.setPorts(parsePortsReverse(egressRule.getPorts()));
            }
            if (!egressRule.getTo().isEmpty()) {
                egress.setSelector(parsePeerReverse(egressRule.getTo().get(0)));
            }
            policyPattern.setEgress(egress);
        }

        if (policy.getSpec().getIngress() != null && !policy.getSpec().getIngress().isEmpty()) {
            NetworkPolicyIngressRule ingressRule = policy.getSpec().getIngress().get(0);
            NetworkPolicyRule egress = new NetworkPolicyRule();
            if (!ingressRule.getPorts().isEmpty()) {
                egress.setPorts(parsePortsReverse(ingressRule.getPorts()));
            }
            if (!ingressRule.getFrom().isEmpty()) {
                egress.setSelector(parsePeerReverse(ingressRule.getFrom().get(0)));
            }
            policyPattern.setIngress(egress);
        }
        return policyPattern;
    }

    private static List<NetworkPolicyPort> parsePorts(List<com.k8s.network.controller.backend.model.networking.NetworkPolicyPort> ports) {
        List<NetworkPolicyPort> portsList = new ArrayList<>();
        for (com.k8s.network.controller.backend.model.networking.NetworkPolicyPort port : ports) {
            portsList.add(new NetworkPolicyPort(new IntOrString(port.getPort()), port.getProtocol()));
        }
        return portsList;
    }

    private static List<com.k8s.network.controller.backend.model.networking.NetworkPolicyPort> parsePortsReverse(List<NetworkPolicyPort> ports) {
        List<com.k8s.network.controller.backend.model.networking.NetworkPolicyPort> portsList = new ArrayList<>();
        for (NetworkPolicyPort port : ports) {
            portsList.add(new com.k8s.network.controller.backend.model.networking.NetworkPolicyPort(port.getPort().getIntVal(), port.getProtocol()));
        }
        return portsList;
    }

    private static NetworkPolicyPeer parsePeer(com.k8s.network.controller.backend.model.networking.NetworkPolicyPeer peer) {
        NetworkPolicyPeer networkPolicyPeer = new NetworkPolicyPeer();
        if (peer.getIpBlock() != null) {
            IPBlock ipBlock = new IPBlock();
            ipBlock.setCidr(peer.getIpBlock().getCidr());
            if (peer.getIpBlock().getExcept() != null) {
                ipBlock.setExcept(Arrays.asList(peer.getIpBlock().getExcept().split(",")));
            }
        }

        if (peer.getNamespaceName() != null) {
            networkPolicyPeer.setNamespaceSelector(new LabelSelector(
                    Collections.singletonList(
                            new LabelSelectorRequirement(
                                    "name",
                                    "In",
                                    Collections.singletonList(peer.getNamespaceName()))
                    ), null)
            );
        }

        if (peer.getPodSelectorLabels() != null) {
            networkPolicyPeer.setPodSelector(new LabelSelector(null, peer.getPodSelectorLabels()));
        }
        return networkPolicyPeer;
    }

    private static com.k8s.network.controller.backend.model.networking.NetworkPolicyPeer parsePeerReverse(NetworkPolicyPeer peer) {
        com.k8s.network.controller.backend.model.networking.NetworkPolicyPeer networkPolicyPeer =
                new com.k8s.network.controller.backend.model.networking.NetworkPolicyPeer();

        if (peer.getIpBlock() != null) {
            com.k8s.network.controller.backend.model.networking.IPBlock ipBlock =
                    new com.k8s.network.controller.backend.model.networking.IPBlock();
            ipBlock.setCidr(peer.getIpBlock().getCidr());
            if (peer.getIpBlock().getExcept() != null) {
                ipBlock.setExcept(String.join(",", peer.getIpBlock().getExcept()));
            }
        }

        if (peer.getNamespaceSelector() != null) {
            LabelSelectorRequirement selectorRequirement = peer.getNamespaceSelector()
                    .getMatchExpressions()
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Selector not found"));
            networkPolicyPeer.setNamespaceName(selectorRequirement.getValues()
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Namespace name Selector not found")));
        }

        if (peer.getPodSelector() != null) {
            networkPolicyPeer.setPodSelectorLabels(peer.getPodSelector().getMatchLabels());
        }
        return networkPolicyPeer;
    }
}
