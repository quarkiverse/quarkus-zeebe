package io.quarkiverse.zeebe.runtime;

import java.util.Arrays;

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

    @Override
    public String toString() {
        return "JobWorkerValue{" +
                "enabled=" + enabled +
                ", type='" + type + '\'' +
                ", name='" + name + '\'' +
                ", timeout=" + timeout +
                ", maxJobsActive=" + maxJobsActive +
                ", requestTimeout=" + requestTimeout +
                ", pollInterval=" + pollInterval +
                ", fetchVariables=" + Arrays.toString(fetchVariables) +
                ", fetchAllVariables=" + fetchAllVariables +
                ", autoComplete=" + autoComplete +
                '}';
    }
}
