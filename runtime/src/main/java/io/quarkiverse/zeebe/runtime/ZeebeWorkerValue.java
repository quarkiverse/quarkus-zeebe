package io.quarkiverse.zeebe.runtime;

public class ZeebeWorkerValue {

    public String type;

    public String name;

    public long timeout;

    public int maxJobsActive;

    public long requestTimeout;

    public long pollInterval;

    public String[] fetchVariables;

    public String clazz;

    public double expBackoffFactor;

    public double expJitterFactor;

    public long expMaxDelay;

    public long expMinDelay;

}
