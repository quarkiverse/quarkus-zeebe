package io.quarkiverse.zeebe.runtime;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import io.camunda.zeebe.client.impl.ZeebeClientBuilderImpl;
import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.annotations.ConfigItem;

/**
 * Zeebe client configuration
 */
@ConfigGroup
public class ZeebeClientRuntimeConfig {

    /**
     * Default client configuration
     */
    static final ZeebeClientBuilderImpl DEFAULT = (ZeebeClientBuilderImpl) new ZeebeClientBuilderImpl()
            .withProperties(new Properties());

    static final String DEFAULT_AUTH_URL = "https://login.cloud.camunda.io/oauth/token";

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
     * Zeebe client OAuth configuration.
     */
    @ConfigItem(name = "oauth")
    public OAuthConfig oauth = new OAuthConfig();

    /**
     * Zeebe client worker type optional configuration.
     */
    @ConfigItem(name = "workers")
    public Map<String, JobHandlerConfig> workers = new HashMap<>();

    /**
     * Auto-complete configuration for all job handlers
     */
    @ConfigItem(name = "auto-complete")
    public AutoCompleteConfig autoComplete = new AutoCompleteConfig();

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
         * Specify a client secret to request an access token.
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
        @ConfigItem(name = "auth-url", defaultValue = DEFAULT_AUTH_URL)
        public String authUrl = DEFAULT_AUTH_URL;

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
         * Specify a path to a certificate with which to validate gateway requests.
         */
        @ConfigItem(name = "cert-path")
        public Optional<String> certPath = Optional.empty();

        /**
         * Overrides the authority used with TLS virtual hosting.
         * Specifically, to override hostname verification in
         * the TLS handshake. It does not change what host is actually connected to.
         */
        @ConfigItem(name = "override-authority")
        public Optional<String> overrideAuthority = Optional.empty();
    }

    /**
     * Zeebe client job configuration.
     */
    @ConfigGroup
    public static class JobConfig {

        /**
         * Client worker maximum active jobs.
         */
        @ConfigItem(name = "max-jobs-active", defaultValue = "32")
        public Integer workerMaxJobsActive = DEFAULT.getDefaultJobWorkerMaxJobsActive();

        /**
         * Client worker number of threads
         */
        @ConfigItem(name = "worker-execution-threads", defaultValue = "1")
        public Integer workerExecutionThreads = DEFAULT.getNumJobWorkerExecutionThreads();

        /**
         * Client worker default name
         */
        @ConfigItem(name = "worker-name", defaultValue = "default")
        public String workerName = DEFAULT.getDefaultJobWorkerName();

        /**
         * Zeebe client request timeout configuration.
         */
        @ConfigItem(name = "request-timeout", defaultValue = "PT45S")
        public Duration requestTimeout = DEFAULT.getDefaultRequestTimeout();

        /**
         * Client worker global type
         */
        @ConfigItem(name = "default-type")
        public Optional<String> defaultType;

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

        /**
         * Sets the backoff supplier. The supplier is called to determine the retry delay after each failed request;
         * the worker then waits until the returned delay has elapsed before sending the next request.
         * Note that this is used only for the polling mechanism - failures in the JobHandler should be handled there,
         * and retried there if need be.
         *
         * Sets the backoff multiplication factor. The previous delay is multiplied by this factor. Default is 1.6.
         *
         * @see io.camunda.zeebe.client.impl.worker.ExponentialBackoffBuilderImpl
         */
        @ConfigItem(name = "exp-backoff-factor", defaultValue = "1.6")
        public double expBackoffFactor = 1.6;

        /**
         * Sets the jitter factor. The next delay is changed randomly within a range of +/- this factor.
         * For example, if the next delay is calculated to be 1s and the jitterFactor is 0.1 then the actual next
         * delay can be somewhere between 0.9 and 1.1s. Default is 0.1
         *
         * @see io.camunda.zeebe.client.impl.worker.ExponentialBackoffBuilderImpl
         */
        @ConfigItem(name = "exp-jitter-factor", defaultValue = "0.1")
        public double expJitterFactor = 0.1;

        /**
         * Sets the maximum retry delay.
         * Note that the jitter may push the retry delay over this maximum. Default is 5000ms.
         *
         * @see io.camunda.zeebe.client.impl.worker.ExponentialBackoffBuilderImpl
         */
        @ConfigItem(name = "exp-max-delay", defaultValue = "5000")
        public long expMaxDelay = TimeUnit.SECONDS.toMillis(5);

        /**
         * Sets the minimum retry delay.
         * Note that the jitter may push the retry delay below this minimum. Default is 50ms.
         *
         * @see io.camunda.zeebe.client.impl.worker.ExponentialBackoffBuilderImpl
         */
        @ConfigItem(name = "exp-min-delay", defaultValue = "50")
        public long expMinDelay = TimeUnit.MILLISECONDS.toMillis(50);
    }

    /**
     * Zeebe handler configuration.
     */
    @ConfigGroup
    public static class JobHandlerConfig {

        /**
         * Zeebe worker enable or disable flag.
         */
        @ConfigItem(name = "enabled")
        public Optional<Boolean> enabled;

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

    /**
     * Command configuration will be use only for the auto-completion of job handler.
     */
    @ConfigGroup
    public static class AutoCompleteConfig {

        /**
         * Maximum retries for the auto-completion command.
         */
        @ConfigItem(name = "max-retries", defaultValue = "20")
        public int maxRetries;

        /**
         * Maximum retries for the auto-completion command.
         */
        @ConfigItem(name = "retry-delay", defaultValue = "50")
        public long retryDelay;

        /**
         * Sets the backoff supplier. The supplier is called to determine the retry delay after each failed request;
         * the worker then waits until the returned delay has elapsed before sending the next request.
         * Note that this is used only for the polling mechanism - failures in the JobHandler should be handled there,
         * and retried there if need be.
         *
         * Sets the backoff multiplication factor. The previous delay is multiplied by this factor. Default is 1.5.
         */
        @ConfigItem(name = "exp-backoff-factor", defaultValue = "1.5")
        public double expBackoffFactor = 1.5;

        /**
         * Sets the jitter factor. The next delay is changed randomly within a range of +/- this factor.
         * For example, if the next delay is calculated to be 1s and the jitterFactor is 0.1 then the actual next
         * delay can be somewhere between 0.9 and 1.1s. Default is 0.2
         */
        @ConfigItem(name = "exp-jitter-factor", defaultValue = "0.2")
        public double expJitterFactor = 0.2;

        /**
         * Sets the maximum retry delay.
         * Note that the jitter may push the retry delay over this maximum. Default is 1000ms.
         */
        @ConfigItem(name = "exp-max-delay", defaultValue = "1000")
        public long expMaxDelay = TimeUnit.SECONDS.toMillis(1);

        /**
         * Sets the minimum retry delay.
         * Note that the jitter may push the retry delay below this minimum. Default is 50ms.
         */
        @ConfigItem(name = "exp-min-delay", defaultValue = "50")
        public long expMinDelay = TimeUnit.MILLISECONDS.toMillis(50);

    }

    /**
     * Zeebe client OAuth configuration.
     */
    @ConfigGroup
    public static class OAuthConfig {

        /**
         * OAuth client secret ID
         */
        @ConfigItem(name = "client-id")
        public Optional<String> clientId = Optional.empty();

        /**
         * Specify a client secret to request an access token.
         */
        @ConfigItem(name = "client-secret")
        public Optional<String> clientSecret;

        /**
         * Authorization server URL
         */
        @ConfigItem(name = "auth-url", defaultValue = DEFAULT_AUTH_URL)
        public String authUrl = DEFAULT_AUTH_URL;

        /**
         * Credentials cache path
         */
        @ConfigItem(name = "credentials-cache-path")
        public Optional<String> credentialsCachePath;

        /**
         * OAuth connect timeout
         */
        @ConfigItem(name = "connect-timeout", defaultValue = "PT5S")
        public Duration connectTimeout;

        /**
         * OAuth read timeout
         */
        @ConfigItem(name = "read-timeout", defaultValue = "PT5S")
        public Duration readTimeout;

        /**
         * Zeebe token audience
         */
        @ConfigItem(name = "token-audience")
        public Optional<String> tokenAudience;
    }

}
