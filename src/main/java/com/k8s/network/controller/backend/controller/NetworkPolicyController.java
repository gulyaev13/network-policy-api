package com.k8s.network.controller.backend.controller;

import com.k8s.network.controller.backend.client.KubernetesClientWrapper;
import com.k8s.network.controller.backend.converter.NetworkPolicyConverter;
import com.k8s.network.controller.backend.model.networking.NetworkPolicyPattern;
import io.fabric8.kubernetes.api.model.networking.NetworkPolicy;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.k8s.network.controller.backend.converter.NetworkPolicyConverter.convertPatternToPolicy;
import static com.k8s.network.controller.backend.converter.NetworkPolicyConverter.convertPolicyToPattern;

@RestController("networkPolicy")
public class NetworkPolicyController {
    @Autowired
    private KubernetesClientWrapper clientWrapper;

    @GetMapping("networkPolicy")
    public List<NetworkPolicyPattern> networkPolicyList(@RequestParam(name = "namespace", required = false) String namespace) {
        KubernetesClient client;
        if (namespace == null || namespace.isEmpty()) {
            client = clientWrapper.getClient();
        } else {
            client = clientWrapper.getClient().inNamespace(namespace);
        }
        return client
                .network()
                .networkPolicies()
                .list()
                .getItems()
                .stream()
                .map(NetworkPolicyConverter::convertPolicyToPattern)
                .collect(Collectors.toList());
    }

    @PostMapping("networkPolicy")
    public NetworkPolicyPattern createOrUpdateNetworkPolicy(@Valid @RequestBody NetworkPolicyPattern pattern) {
        System.err.println("Received pattern: " + pattern);
        NetworkPolicy networkPolicy = convertPatternToPolicy(pattern);
        System.err.println("Pattern converted to NetworkPolicy: " + networkPolicy);
        return convertPolicyToPattern(clientWrapper
                .getClient()
                .inNamespace(pattern.getNamespace())
                .network()
                .networkPolicies()
                .createOrReplace(networkPolicy));
    }

    @DeleteMapping("networkPolicy")
    public void deleteNetworkPolicy(@RequestParam(name = "namespace", required = false) String namespace, @RequestParam(name = "name", required = false) String name) {
        KubernetesClient client = clientWrapper
                .getClient()
                .inNamespace(namespace);

        Optional<NetworkPolicy> policyOptional = client
                .network()
                .networkPolicies()
                .list()
                .getItems()
                .stream()
                .filter(policy -> policy.getMetadata().getName().equals(name))
                .findFirst();
        if (!policyOptional.isPresent()){
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    String.format("NetworkPolicy '%s' not found in namespace '%s'", name, namespace)
            );
        }

        client.network()
                .networkPolicies()
                .delete(policyOptional.get());
    }
}
