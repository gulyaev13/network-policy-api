package com.k8s.network.controller.backend.model.networking;

import lombok.AllArgsConstructor;
import lombok.Data;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;

@Data
@AllArgsConstructor
public class NetworkPolicyPort {
    @Min(1)
    @Max(65535)
    private int port;
    @Pattern(regexp = "TCP|UDP")
    private String protocol;
}
