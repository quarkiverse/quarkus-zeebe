package io.quarkiverse.zeebe.runtime;

import java.net.URI;
import java.time.Duration;
import java.util.*;

import io.camunda.zeebe.client.api.command.CommandWithTenantStep;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

/**
 * Zeebe client configuration
 */
public interface ZeebeClientRuntimeConfig {

    /**
     * Default client configuration
     */
    String DEFAULT_AUTH_URL = "https://login.cloud.camunda.io/oauth/token";

    /**
     * Zeebe client broker configuration.
     */
    @WithName("broker")
    BrokerConfig broker();

    /**
     * Zeebe client cloud configuration.
     */
    @WithName("cloud")
    CloudConfig cloud();

    /**
     * Zeebe client OAuth configuration.
     */
    @WithName("oauth")
    OAuthConfig oauth();

    /**
     * Zeebe client worker type optional configuration.
     */
    @WithName("workers")
    Map<String, JobHandlerConfig> workers();

    /**
     * Auto-complete configuration for all job handlers
     */
    @WithName("auto-complete")
    AutoCompleteConfig autoComplete();

    /**
     * Zeebe client message configuration.
     */
    @WithName("message")
    MessageConfig message();

    /**
     * Zeebe client security configuration.
     */
    @WithName("security")
    SecurityConfig security();

    /**
     * Zeebe client job configuration.
     */
    @WithName("job")
    JobConfig job();

    /**
     * Zeebe client tracing configuration.
     */
    @WithName("tracing")
    TracingConfig tracing();

    /**
     * Zeebe client tenant configuration.
     */
    @WithName("tenant")
    TenantConfig tenant();

    /**
     * Zeebe client tenant configuration.
     */
    interface TenantConfig {

        /**
         * Zeebe client tenant ID.
         * The tenant identifier which is used for tenant-aware commands when no tenant identifier is set.
         */
        @WithName("default-tenant-id")
        @WithDefault(CommandWithTenantStep.DEFAULT_TENANT_IDENTIFIER)
        String defaultTenantId();

        /**
         * Zeebe client default job worker tenant ID's.
         * The tenant identifiers which are used for job-activation commands when no tenant identifiers are set.
         */
        @WithName("default-job-worker-tenant-ids")
        @WithDefault(CommandWithTenantStep.DEFAULT_TENANT_IDENTIFIER)
        List<String> defaultJobWorkerTenantIds();
    }

    /**
     * Zeebe client broker configuration.
     */
    interface BrokerConfig {

        /**
         * Zeebe gateway address.
         * Default: localhost:26500
         */
        @WithName("gateway-address")
        @WithDefault("localhost:26500")
        String gatewayAddress();

        /**
         * Zeebe gateway rest address.
         * Default: localhost:8080
         */
        @WithName("rest-address")
        @WithDefault("http://0.0.0.0:8080")
        URI restAddress();

        /**
         * Client keep alive duration
         */
        @WithName("keep-alive")
        @WithDefault("PT45S")
        Duration keepAlive();
    }

    /**
     * Zeebe client cloud configuration.
     */
    interface CloudConfig {

        /**
         * Cloud cluster ID
         */
        @WithName("cluster-id")
        Optional<String> clusterId();

        /**
         * Cloud client secret ID
         */
        @WithName("client-id")
        Optional<String> clientId();;

        /**
         * Specify a client secret to request an access token.
         */
        @WithName("client-secret")
        Optional<String> clientSecret();

        /**
         * Cloud region
         */
        @WithName("region")
        @WithDefault("bru-2")
        String region();

        /**
         * Cloud base URL
         */
        @WithName("base-url")
        @WithDefault("zeebe.camunda.io")
        String baseUrl();

        /**
         * Cloud authorization server URL
         */
        @WithName("auth-url")
        @WithDefault(DEFAULT_AUTH_URL)
        String authUrl();

        /**
         * Cloud port
         */
        @WithName("port")
        @WithDefault("443")
        int port();

        /**
         * Cloud credentials cache path
         */
        @WithName("credentials-cache-path")
        Optional<String> credentialsCachePath();

    }

    /**
     * Zeebe client message configuration.
     */
    interface MessageConfig {

        /**
         * Client message time to live duration.
         */
        @WithName("time-to-live")
        @WithDefault("PT1H")
        Duration timeToLive();
    }

    /**
     * Zeebe client security configuration.
     */
    interface SecurityConfig {

        /**
         * Client security plaintext flag.
         */
        @WithName("plaintext")
        @WithDefault("true")
        boolean plaintext();

        /**
         * Specify a path to a certificate with which to validate gateway requests.
         */
        @WithName("cert-path")
        Optional<String> certPath();

        /**
         * Overrides the authority used with TLS virtual hosting.
         * Specifically, to override hostname verification in
         * the TLS handshake. It does not change what host is actually connected to.
         */
        @WithName("override-authority")
        Optional<String> overrideAuthority();
    }

    /**
     * Zeebe client job configuration.
     */
    interface JobConfig {

        /**
         * Client worker maximum active jobs.
         */
        @WithName("max-jobs-active")
        @WithDefault("32")
        Integer workerMaxJobsActive();

        /**
         * Client worker number of threads
         */
        @WithName("worker-execution-threads")
        @WithDefault("1")
        Integer workerExecutionThreads();

        /**
         * Client worker default name
         */
        @WithName("worker-name")
        @WithDefault("default")
        String workerName();

        /**
         * Zeebe client request timeout configuration.
         */
        @WithName("request-timeout")
        @WithDefault("PT45S")
        Duration requestTimeout();

        /**
         * Client worker global type
         */
        @WithName("default-type")
        Optional<String> defaultType();

        /**
         * Client job timeout
         */
        @WithName("timeout")
        @WithDefault("PT5M")
        Duration timeout();

        /**
         * Client job poll interval
         */
        @WithName("poll-interval")
        @WithDefault("PT0.100S")
        Duration pollInterval();

        /**
         * Sets the backoff supplier. The supplier is called to determine the retry delay after each failed request;
         * the worker then waits until the returned delay has elapsed before sending the next request.
         * Note that this is used only for the polling mechanism - failures in the JobHandler should be handled there,
         * and retried there if need be.
         * Sets the backoff multiplication factor. The previous delay is multiplied by this factor. Default is 1.6.
         *
         * @see io.camunda.zeebe.client.impl.worker.ExponentialBackoffBuilderImpl
         */
        @WithName("exp-backoff-factor")
        @WithDefault("1.6")
        double expBackoffFactor();

        /**
         * Sets the jitter factor. The next delay is changed randomly within a range of +/- this factor.
         * For example, if the next delay is calculated to be 1s and the jitterFactor is 0.1 then the actual next
         * delay can be somewhere between 0.9 and 1.1s. Default is 0.1
         *
         * @see io.camunda.zeebe.client.impl.worker.ExponentialBackoffBuilderImpl
         */
        @WithName("exp-jitter-factor")
        @WithDefault("0.1")
        double expJitterFactor();

        /**
         * Sets the maximum retry delay.
         * Note that the jitter may push the retry delay over this maximum. Default is 5000ms.
         *
         * @see io.camunda.zeebe.client.impl.worker.ExponentialBackoffBuilderImpl
         */
        @WithName("exp-max-delay")
        @WithDefault("5000")
        long expMaxDelay();

        /**
         * Sets the minimum retry delay.
         * Note that the jitter may push the retry delay below this minimum. Default is 50ms.
         *
         * @see io.camunda.zeebe.client.impl.worker.ExponentialBackoffBuilderImpl
         */
        @WithName("exp-min-delay")
        @WithDefault("50")
        long expMinDelay();
    }

    /**
     * Zeebe handler configuration.
     */
    interface JobHandlerConfig {

        /**
         * Zeebe worker enable or disable flag.
         */
        @WithName("enabled")
        Optional<Boolean> enabled();

        /**
         * Zeebe worker handler name.
         */
        @WithName("name")
        Optional<String> name();

        /**
         * Zeebe worker timeout.
         */
        @WithName("timeout")
        Optional<Long> timeout();

        /**
         * Zeebe worker maximum jobs active.
         */
        @WithName("max-jobs-active")
        Optional<Integer> maxJobsActive();

        /**
         * Zeebe worker request timeout.
         */
        @WithName("request-timeout")
        Optional<Long> requestTimeout();

        /**
         * Zeebe worker poll interval.
         */
        @WithName("poll-interval")
        Optional<Long> pollInterval();

    }

    /**
     * Zeebe client tracing configuration.
     */
    interface TracingConfig {

        /**
         * List of span names
         */
        @WithName("attributes")
        Optional<List<String>> attributes();
    }

    /**
     * Command configuration will be use only for the auto-completion of job handler.
     */
    interface AutoCompleteConfig {

        /**
         * Maximum retries for the auto-completion command.
         */
        @WithName("max-retries")
        @WithDefault("20")
        int maxRetries();

        /**
         * Maximum retries for the auto-completion command.
         */
        @WithName("retry-delay")
        @WithDefault("50")
        long retryDelay();

        /**
         * Sets the backoff supplier. The supplier is called to determine the retry delay after each failed request;
         * the worker then waits until the returned delay has elapsed before sending the next request.
         * Note that this is used only for the polling mechanism - failures in the JobHandler should be handled there,
         * and retried there if need be.
         * Sets the backoff multiplication factor. The previous delay is multiplied by this factor. Default is 1.5.
         */
        @WithName("exp-backoff-factor")
        @WithDefault("1.5")
        double expBackoffFactor();

        /**
         * Sets the jitter factor. The next delay is changed randomly within a range of +/- this factor.
         * For example, if the next delay is calculated to be 1s and the jitterFactor is 0.1 then the actual next
         * delay can be somewhere between 0.9 and 1.1s. Default is 0.2
         */
        @WithName("exp-jitter-factor")
        @WithDefault("0.2")
        double expJitterFactor();

        /**
         * Sets the maximum retry delay.
         * Note that the jitter may push the retry delay over this maximum. Default is 1000ms.
         */
        @WithName("exp-max-delay")
        @WithDefault("1000")
        long expMaxDelay();

        /**
         * Sets the minimum retry delay.
         * Note that the jitter may push the retry delay below this minimum. Default is 50ms.
         */
        @WithName("exp-min-delay")
        @WithDefault("50")
        long expMinDelay();

    }

    /**
     * Zeebe client OAuth configuration.
     */
    interface OAuthConfig {

        /**
         * OAuth client secret ID
         */
        @WithName("client-id")
        Optional<String> clientId();

        /**
         * Specify a client secret to request an access token.
         */
        @WithName("client-secret")
        Optional<String> clientSecret();

        /**
         * Authorization server URL
         */
        @WithName("auth-url")
        @WithDefault(DEFAULT_AUTH_URL)
        String authUrl();

        /**
         * Credentials cache path
         */
        @WithName("credentials-cache-path")
        Optional<String> credentialsCachePath();

        /**
         * OAuth connect timeout
         */
        @WithName("connect-timeout")
        @WithDefault("PT5S")
        Duration connectTimeout();

        /**
         * OAuth read timeout
         */
        @WithName("read-timeout")
        @WithDefault("PT5S")
        Duration readTimeout();

        /**
         * Zeebe token audience
         */
        @WithName("token-audience")
        Optional<String> tokenAudience();
    }

}
