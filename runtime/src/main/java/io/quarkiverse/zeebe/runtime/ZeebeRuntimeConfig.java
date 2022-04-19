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
    public BrokerConfig broker = new BrokerConfig();

    /**
     * Zeebe client cloud configuration.
     */
    @ConfigItem(name = "cloud")
    public CloudConfig cloud = new CloudConfig();

    /**
     * Zeebe client worker configuration.
     */
    @ConfigItem(name = "worker")
    public WorkerConfig worker = new WorkerConfig();

    /**
     * Zeebe client worker type optional configuration.
     */
    @ConfigItem(name = "workers")
    public Map<String, HandlerConfig> workers = new HashMap<>();

    /**
     * Zeebe client message configuration.
     */
    @ConfigItem(name = "message")
    public MessageConfig message = new MessageConfig();

    /**
     * Zeebe client security configuration.
     */
    @ConfigItem(name = "security")
    public SecurityConfig security = new SecurityConfig();

    /**
     * Zeebe client job configuration.
     */
    @ConfigItem(name = "job")
    public JobConfig job = new JobConfig();

    /**
     * Zeebe client request timeout configuration.
     */
    @ConfigItem(name = "request-timeout", defaultValue = "PT45S")
    public Duration requestTimeout = DEFAULT.getDefaultRequestTimeout();

    /**
     * Zeebe client tracing configuration.
     */
    @ConfigItem(name = "tracing")
    public TracingConfig tracing = new TracingConfig();

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
        public String gatewayAddress;

        /**
         * Client keep alive duration
         */
        @ConfigItem(name = "keep-alive", defaultValue = "PT45S")
        public Duration keepAlive = DEFAULT.getKeepAlive();
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
        public Optional<String> clusterId = Optional.empty();

        /**
         * Cloud client secret ID
         */
        @ConfigItem(name = "client-id")
        public Optional<String> clientId = Optional.empty();

        /**
         * Cloud client secret
         */
        @ConfigItem(name = "client-secret")
        public Optional<String> clientSecret;

        /**
         * Cloud region
         */
        @ConfigItem(name = "region", defaultValue = "bru-2")
        public String region = "bru-2";

        /**
         * Cloud base URL
         */
        @ConfigItem(name = "base-url", defaultValue = "zeebe.camunda.io")
        public String baseUrl = "zeebe.camunda.io";

        /**
         * Cloud authorization server URL
         */
        @ConfigItem(name = "auth-url", defaultValue = "https://login.cloud.camunda.io/oauth/token")
        public String authUrl = "https://login.cloud.camunda.io/oauth/token";

        /**
         * Cloud port
         */
        @ConfigItem(name = "port", defaultValue = "443")
        public int port = 443;

        /**
         * Cloud credentials cache path
         */
        @ConfigItem(name = "credentials-cache-path")
        public Optional<String> credentialsCachePath;

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
        public Integer maxJobsActive = DEFAULT.getDefaultJobWorkerMaxJobsActive();

        /**
         * Client worker number of threads
         */
        @ConfigItem(name = "threads", defaultValue = "1")
        public Integer threads = DEFAULT.getNumJobWorkerExecutionThreads();

        /**
         * Client worker default name
         */
        @ConfigItem(name = "default-name", defaultValue = "default")
        public String defaultName = DEFAULT.getDefaultJobWorkerName();

        /**
         * Client worker global type
         */
        @ConfigItem(name = "default-type")
        public Optional<String> defaultType;
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
        public Duration timeToLive = DEFAULT.getDefaultMessageTimeToLive();
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
        public boolean plaintext = true;

        /**
         * CA certificate path
         */
        @ConfigItem(name = "cert-path")
        public Optional<String> certPath = Optional.empty();
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
        public Duration timeout = DEFAULT.getDefaultJobTimeout();

        /**
         * Client job pool interval
         */
        @ConfigItem(name = "pool-interval", defaultValue = "PT0.100S")
        public Duration pollInterval = DEFAULT.getDefaultJobPollInterval();
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
        public Optional<String> name;

        /**
         * Zeebe worker timeout.
         */
        @ConfigItem(name = "timeout")
        public Optional<Long> timeout;

        /**
         * Zeebe worker maximum jobs active.
         */
        @ConfigItem(name = "max-jobs-active")
        public Optional<Integer> maxJobsActive;

        /**
         * Zeebe worker request timeout.
         */
        @ConfigItem(name = "request-timeout")
        public Optional<Long> requestTimeout;

        /**
         * Zeebe worker poll interval.
         */
        @ConfigItem(name = "poll-interval")
        public Optional<Long> pollInterval;

        /**
         * Zeebe worker fetch variables.
         */
        @ConfigItem(name = "fetch-variables")
        public Optional<List<String>> fetchVariables;

        /**
         * Sets the backoff supplier. The supplier is called to determine the retry delay after each failed request;
         * the worker then waits until the returned delay has elapsed before sending the next request.
         * Note that this is used only for the polling mechanism - failures in the JobHandler should be handled there,
         * and retried there if need be.
         *
         * Sets the backoff multiplication factor. The previous delay is multiplied by this factor. Default is 1.6.
         */
        @ConfigItem(name = "exp-backoff-factor")
        public Optional<Double> expBackoffFactor;

        /**
         * Sets the jitter factor. The next delay is changed randomly within a range of +/- this factor.
         * For example, if the next delay is calculated to be 1s and the jitterFactor is 0.1 then the actual next
         * delay can be somewhere between 0.9 and 1.1s. Default is 0.1
         */
        @ConfigItem(name = "exp-jitter-factor")
        public Optional<Double> expJitterFactor;

        /**
         * Sets the maximum retry delay.
         * Note that the jitter may push the retry delay over this maximum. Default is 5000ms.
         */
        @ConfigItem(name = "exp-max-delay")
        public Optional<Long> expMaxDelay;

        /**
         * Sets the minimum retry delay.
         * Note that the jitter may push the retry delay below this minimum. Default is 50ms.
         */
        @ConfigItem(name = "exp-min-delay")
        public Optional<Long> expMinDelay;
    }

    /**
     * Zeebe client tracing configuration.
     */
    @ConfigGroup
    public static class TracingConfig {

        /**
         * List of span names
         */
        @ConfigItem(name = "attributes")
        public Optional<List<String>> attributes;
    }
}
