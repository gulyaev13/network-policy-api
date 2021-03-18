package com.k8s.network.controller.backend.model.networking;

import lombok.Data;

import javax.validation.constraints.Pattern;

@Data
public class IPBlock {
    private String cidr;
    private String except;
}
