package com.helei.dto.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.net.InetSocketAddress;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProxyConfig implements Serializable {

    private String hostname;

    private int port;

    public InetSocketAddress getProxyAddress() {
        return new InetSocketAddress(hostname, port);
    }
}
