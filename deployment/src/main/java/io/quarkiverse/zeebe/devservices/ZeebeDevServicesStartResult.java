package io.quarkiverse.zeebe.devservices;

import java.io.Closeable;

public class ZeebeDevServicesStartResult {
    public final String gateway;
    public final String hazelcast;
    final Closeable closeable;

    public ZeebeDevServicesStartResult(String gateway, String hazelcast, Closeable closeable) {
        this.gateway = gateway;
        this.hazelcast = hazelcast;
        this.closeable = closeable;
    }
}
