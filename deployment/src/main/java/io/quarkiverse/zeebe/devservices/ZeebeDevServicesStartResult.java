package io.quarkiverse.zeebe.devservices;

import java.io.Closeable;

public class ZeebeDevServicesStartResult {
    public final String gateway;
    public final String client;
    public final String hazelcast;
    final Closeable closeable;

    public ZeebeDevServicesStartResult(String gateway, String client, String hazelcast, Closeable closeable) {
        this.gateway = gateway;
        this.client = client;
        this.hazelcast = hazelcast;
        this.closeable = closeable;
    }
}
