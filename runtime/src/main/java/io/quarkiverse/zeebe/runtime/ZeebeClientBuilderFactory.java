package io.quarkiverse.zeebe.runtime;

import io.camunda.zeebe.client.CredentialsProvider;
import io.camunda.zeebe.client.ZeebeClientBuilder;
import io.camunda.zeebe.client.impl.ZeebeClientBuilderImpl;
import io.camunda.zeebe.client.impl.oauth.OAuthCredentialsProviderBuilder;

public class ZeebeClientBuilderFactory {

    public static ZeebeClientBuilder createBuilder(ZeebeRuntimeConfig config) {
        ZeebeClientBuilderImpl builder = new ZeebeClientBuilderImpl();
        builder.gatewayAddress(createGatewayAddress(config))
                .defaultJobPollInterval(config.job.pollInterval)
                .defaultJobTimeout(config.job.timeout)
                .defaultJobWorkerMaxJobsActive(config.job.workerMaxJobsActive)
                .defaultJobWorkerName(config.job.workerName)
                .defaultMessageTimeToLive(config.message.timeToLive)
                .numJobWorkerExecutionThreads(config.job.workerExecutionThreads)
                .defaultRequestTimeout(config.job.requestTimeout)
                .credentialsProvider(getCredentialsProvider(config.cloud));

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

    private static CredentialsProvider getCredentialsProvider(ZeebeRuntimeConfig.CloudConfig config) {
        if (config.clientId.isPresent() && config.clientSecret.isPresent() && config.clusterId.isPresent()) {
            OAuthCredentialsProviderBuilder builder = CredentialsProvider.newCredentialsProviderBuilder();
            builder.authorizationServerUrl(config.authUrl);
            config.clientId.ifPresent(builder::clientId);
            config.clientSecret.ifPresent(builder::clientSecret);
            config.credentialsCachePath.ifPresent(builder::credentialsCachePath);
            builder.audience(String.format("%s.%s.%s", config.clusterId.get(), config.region, config.baseUrl));
            return builder.build();
        }
        return null;
    }
}
