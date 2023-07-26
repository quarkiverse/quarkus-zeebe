package io.quarkiverse.zeebe.runtime;

import io.camunda.zeebe.client.CredentialsProvider;
import io.camunda.zeebe.client.ZeebeClientBuilder;
import io.camunda.zeebe.client.impl.ZeebeClientBuilderImpl;
import io.camunda.zeebe.client.impl.oauth.OAuthCredentialsProviderBuilder;

public class ZeebeClientBuilderFactory {

    public static ZeebeClientBuilder createBuilder(ZeebeRuntimeConfig config) {
        ZeebeClientBuilderImpl builder = new ZeebeClientBuilderImpl();

        builder.gatewayAddress(createGatewayAddress(config))
                .keepAlive(config.broker.keepAlive)
                .defaultJobPollInterval(config.job.pollInterval)
                .defaultJobTimeout(config.job.timeout)
                .defaultJobWorkerMaxJobsActive(config.job.workerMaxJobsActive)
                .defaultJobWorkerName(config.job.workerName)
                .defaultMessageTimeToLive(config.message.timeToLive)
                .numJobWorkerExecutionThreads(config.job.workerExecutionThreads)
                .defaultRequestTimeout(config.job.requestTimeout)
                .credentialsProvider(getCredentialsProvider(config));

        config.security.overrideAuthority.ifPresent(builder::overrideAuthority);
        config.security.certPath.ifPresent(builder::caCertificatePath);
        if (config.security.plaintext) {
            builder.usePlaintext();
        }
        return builder;
    }

    private static String createGatewayAddress(ZeebeRuntimeConfig config) {
        if (config.cloud.clusterId.isPresent()) {
            return String.format("%s.%s.%s:%d",
                    config.cloud.clusterId.get(),
                    config.cloud.region,
                    config.cloud.baseUrl,
                    config.cloud.port);
        }
        return config.broker.gatewayAddress;
    }

    private static CredentialsProvider getCredentialsProvider(ZeebeRuntimeConfig config) {
        ZeebeRuntimeConfig.CloudConfig cloud = config.cloud;
        if (cloud.clientId.isPresent() && cloud.clientSecret.isPresent() && cloud.clusterId.isPresent()) {
            OAuthCredentialsProviderBuilder builder = CredentialsProvider.newCredentialsProviderBuilder();
            builder.authorizationServerUrl(cloud.authUrl);
            cloud.clientId.ifPresent(builder::clientId);
            cloud.clientSecret.ifPresent(builder::clientSecret);
            cloud.credentialsCachePath.ifPresent(builder::credentialsCachePath);
            builder.audience(String.format("%s.%s.%s", cloud.clusterId.get(), cloud.region, cloud.baseUrl));
            return builder.build();
        }

        ZeebeRuntimeConfig.OAuthConfig oauth = config.oauth;
        if (oauth.clientId.isPresent() && oauth.clientSecret.isPresent()) {
            OAuthCredentialsProviderBuilder builder = CredentialsProvider.newCredentialsProviderBuilder();
            builder.authorizationServerUrl(oauth.authUrl);
            oauth.clientId.ifPresent(builder::clientId);
            oauth.clientSecret.ifPresent(builder::clientSecret);
            oauth.credentialsCachePath.ifPresent(builder::credentialsCachePath);

            // remove port from the gateway address
            int index = config.broker.gatewayAddress.lastIndexOf(':');
            if (index > 0) {
                builder.audience(config.broker.gatewayAddress.substring(0, index));
            }

            // setup connection timeout
            builder.connectTimeout(oauth.connectTimeout);
            builder.readTimeout(oauth.readTimeout);
            return builder.build();
        }
        return null;
    }

}
