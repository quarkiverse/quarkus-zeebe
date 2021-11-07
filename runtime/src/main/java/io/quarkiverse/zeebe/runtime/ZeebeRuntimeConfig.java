package io.quarkiverse.zeebe.runtime;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import io.camunda.zeebe.client.impl.ZeebeClientBuilderImpl;
import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(name = "zeebe.client", phase = ConfigPhase.RUN_TIME)
public class ZeebeRuntimeConfig {

    /**
     * Default client configuration
     */
    static final ZeebeClientBuilderImpl DEFAULT = (ZeebeClientBuilderImpl) new ZeebeClientBuilderImpl()
            .withProperties(new Properties());

    /**
     * Zeebe client broker configuration.
     */
    @ConfigItem(name = "broker")
    BrokerConfig broker = new BrokerConfig();

    /**
     * Zeebe client cloud configuration.
     */
    @ConfigItem(name = "cloud")
    CloudConfig cloud = new CloudConfig();

    /**
     * Zeebe client worker configuration.
     */
    @ConfigItem(name = "worker")
    WorkerConfig worker = new WorkerConfig();

    /**
     * Zeebe client worker type optional configuration.
     */
    @ConfigItem(name = "workers")
    Map<String, HandlerConfig> workers = new HashMap<>();

    /**
     * Zeebe client message configuration.
     */
    @ConfigItem(name = "message")
    MessageConfig message = new MessageConfig();

    /**
     * Zeebe client security configuration.
     */
    @ConfigItem(name = "security")
    SecurityConfig security = new SecurityConfig();

    /**
     * Zeebe client job configuration.
     */
    @ConfigItem(name = "job")
    JobConfig job = new JobConfig();

    /**
     * Zeebe client request timeout configuration.
     */
    @ConfigItem(name = "request-timeout", defaultValue = "PT45S")
    Duration requestTimeout = DEFAULT.getDefaultRequestTimeout();

    /**
     * Zeebe client broker configuration.
     */
    @ConfigGroup
    public static class BrokerConfig {

        /**
         * Zeebe gateway address.
         * Default: localhost:26500
         */
        @ConfigItem(name = "gateway-address", defaultValue = "localhost:26500")
        String gatewayAddress;

        /**
         * Client keep alive duration
         */
        @ConfigItem(name = "keep-alive", defaultValue = "PT45S")
        Duration keepAlive = DEFAULT.getKeepAlive();
    }

    /**
     * Zeebe client cloud configuration.
     */
    @ConfigGroup
    public static class CloudConfig {

        /**
         * Cloud cluster ID
         */
        @ConfigItem(name = "cluster-id")
        Optional<String> clusterId;

        /**
         * Cloud client secret ID
         */
        @ConfigItem(name = "client-id")
        Optional<String> clientId;

        /**
         * Cloud client secret
         */
        @ConfigItem(name = "client-secret")
        Optional<String> clientSecret;

        /**
         * Cloud region
         */
        @ConfigItem(name = "region", defaultValue = "bru-2")
        String region = "bru-2";

        /**
         * Cloud base URL
         */
        @ConfigItem(name = "base-url", defaultValue = "zeebe.camunda.io")
        String baseUrl = "zeebe.camunda.io";

        /**
         * Cloud authorization server URL
         */
        @ConfigItem(name = "auth-url", defaultValue = "https://login.cloud.camunda.io/oauth/token")
        String authUrl = "https://login.cloud.camunda.io/oauth/token";

        /**
         * Cloud port
         */
        @ConfigItem(name = "port", defaultValue = "443")
        int port = 443;

        /**
         * Cloud credentials cache path
         */
        @ConfigItem(name = "credentials-cache-path")
        Optional<String> credentialsCachePath;

    }

    /**
     * Zeebe client worker configuration.
     */
    @ConfigGroup
    public static class WorkerConfig {

        /**
         * Client worker maximum active jobs.
         */
        @ConfigItem(name = "max-jobs-active", defaultValue = "32")
        Integer maxJobsActive = DEFAULT.getDefaultJobWorkerMaxJobsActive();

        /**
         * Client worker number of threads
         */
        @ConfigItem(name = "threads", defaultValue = "1")
        Integer threads = DEFAULT.getNumJobWorkerExecutionThreads();

        /**
         * Client worker default name
         */
        @ConfigItem(name = "default-name", defaultValue = "default")
        String defaultName = DEFAULT.getDefaultJobWorkerName();

        /**
         * Client worker global type
         */
        @ConfigItem(name = "default-type")
        Optional<String> defaultType;
    }

    /**
     * Zeebe client message configuration.
     */
    @ConfigGroup
    public static class MessageConfig {

        /**
         * Client message time to live duration.
         */
        @ConfigItem(name = "time-to-live", defaultValue = "PT1H")
        Duration timeToLive = DEFAULT.getDefaultMessageTimeToLive();
    }

    /**
     * Zeebe client security configuration.
     */
    @ConfigGroup
    public static class SecurityConfig {

        /**
         * Client security plaintext flag.
         */
        @ConfigItem(name = "plaintext", defaultValue = "true")
        boolean plaintext;

        /**
         * CA certificate path
         */
        @ConfigItem(name = "cert-path")
        Optional<String> certPath;
    }

    /**
     * Zeebe client job configuration.
     */
    @ConfigGroup
    public static class JobConfig {

        /**
         * Client job timeout
         */
        @ConfigItem(name = "timeout", defaultValue = "PT5M")
        Duration timeout = DEFAULT.getDefaultJobTimeout();

        /**
         * Client job pool interval
         */
        @ConfigItem(name = "pool-interval", defaultValue = "PT0.100S")
        Duration pollInterval = DEFAULT.getDefaultJobPollInterval();
    }

    /**
     * Zeebe handler configuration.
     */
    @ConfigGroup
    public static class HandlerConfig {

        /**
         * Zeebe worker handler name.
         */
        @ConfigItem(name = "name")
        Optional<String> name;

        /**
         * Zeebe worker timeout.
         */
        @ConfigItem(name = "timeout")
        Optional<Long> timeout;

        /**
         * Zeebe worker maximum jobs active.
         */
        @ConfigItem(name = "max-jobs-active")
        Optional<Integer> maxJobsActive;

        /**
         * Zeebe worker request timeout.
         */
        @ConfigItem(name = "request-timeout")
        Optional<Long> requestTimeout;

        /**
         * Zeebe worker poll interval.
         */
        @ConfigItem(name = "poll-interval")
        Optional<Long> pollInterval;

        /**
         * Zeebe worker fetch variables.
         */
        @ConfigItem(name = "fetch-variables")
        Optional<List<String>> fetchVariables;

        /**
         * Sets the backoff supplier configuration.
         */
        @ConfigItem(name = "exponential-backoff")
        ExponentialBackoffConfig exponentialBackoff = new ExponentialBackoffConfig();
    }

    /**
     * Sets the backoff supplier. The supplier is called to determine the retry delay after each failed request;
     * the worker then waits until the returned delay has elapsed before sending the next request.
     * Note that this is used only for the polling mechanism - failures in the JobHandler should be handled there,
     * and retried there if need be.
     */
    @ConfigGroup
    public static class ExponentialBackoffConfig {
        /**
         * Sets the backoff multiplication factor. The previous delay is multiplied by this factor. Default is 1.6.
         */
        @ConfigItem(name = "backoff-factor")
        Optional<Double> backoffFactor;

        /**
         * Sets the jitter factor. The next delay is changed randomly within a range of +/- this factor.
         * For example, if the next delay is calculated to be 1s and the jitterFactor is 0.1 then the actual next
         * delay can be somewhere between 0.9 and 1.1s. Default is 0.1
         */
        @ConfigItem(name = "jitter-factor")
        Optional<Double> jitterFactor;

        /**
         * Sets the maximum retry delay.
         * Note that the jitter may push the retry delay over this maximum. Default is 5000ms.
         */
        @ConfigItem(name = "max-delay")
        Optional<Long> maxDelay;

        /**
         * Sets the minimum retry delay.
         * Note that the jitter may push the retry delay below this minimum. Default is 50ms.
         */
        @ConfigItem(name = "min-delay")
        Optional<Long> minDelay;
    }
}
