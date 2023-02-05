package io.quarkiverse.zeebe;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
@Documented
public @interface JobWorker {

    /**
     * Enable or disable the job worker.
     */
    boolean enabled() default true;

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
     * Fetch all variables for the job.
     *
     * @return disable or enable fetch all variables.
     */
    boolean fetchAllVariables() default false;

    /**
     * Auto-complete enable or disable feature.
     *
     * @return the auto-complete enable or disable feature.
     */
    boolean autoComplete() default true;

}
