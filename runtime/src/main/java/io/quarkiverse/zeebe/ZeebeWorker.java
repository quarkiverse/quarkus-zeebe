package io.quarkiverse.zeebe;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.FIELD })
public @interface ZeebeWorker {

    /**
     * Job type.
     *
     * @return type for which the JobHandler has to be registered
     */
    String type() default "";

    /**
     * Name of the job handler.
     *
     * @return name for zeebe job handler
     */
    String name() default "";

    /**
     * Job handler timeout
     *
     * @return job handler timeout
     */
    long timeout() default -1L;

    /**
     * Maximum active jobs.
     *
     * @return maximum active jobs.
     */
    int maxJobsActive() default -1;

    /**
     * Request timeout for the job
     *
     * @return request timeout for the job.
     */
    long requestTimeout() default -1L;

    /**
     * Poll interval for the job worker.
     *
     * @return job poll interval.
     */
    long pollInterval() default -1L;

    /**
     * List of fetch variables for the job.
     *
     * @return list of fetch variables for the job.
     */
    String[] fetchVariables() default {};

    /**
     * Exponential backoff configuration.
     * 
     * @return exponential backoff custom configuration
     */
    ExponentialBackoff exponentialBackoff() default @ExponentialBackoff();

    /**
     * Exponential backoff configuration.
     */
    @interface ExponentialBackoff {

        /**
         * Sets the backoff multiplication factor. The previous delay is multiplied by this factor. Default is 1.6.
         */
        double backoffFactor() default -1;

        /**
         * Sets the jitter factor. The next delay is changed randomly within a range of +/- this factor.
         * For example, if the next delay is calculated to be 1s and the jitterFactor is 0.1 then the actual next
         * delay can be somewhere between 0.9 and 1.1s.
         */
        double jitterFactor() default -1;

        /**
         * Sets the maximum retry delay.
         * Note that the jitter may push the retry delay over this maximum. Default is 5000ms.
         */
        long maxDelay() default -1L;

        /**
         * Sets the minimum retry delay.
         * Note that the jitter may push the retry delay below this minimum. Default is 50ms.
         */
        long minDelay() default -1L;
    }
}
