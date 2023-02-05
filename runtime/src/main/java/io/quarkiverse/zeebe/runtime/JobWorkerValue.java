package io.quarkiverse.zeebe.runtime;

public class JobWorkerValue {

    public boolean enabled;

    public String type;

    public String name;

    public long timeout;

    public int maxJobsActive;

    public long requestTimeout;

    public long pollInterval;

    public String[] fetchVariables;

    public boolean fetchAllVariables;

    public boolean autoComplete;

}
