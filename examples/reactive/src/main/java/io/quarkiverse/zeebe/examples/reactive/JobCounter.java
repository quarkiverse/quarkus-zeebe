package io.quarkiverse.zeebe.examples.reactive;

import java.util.concurrent.atomic.AtomicInteger;

import javax.enterprise.context.ApplicationScoped;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class JobCounter {

    private static final Logger log = LoggerFactory.getLogger(JobCounter.class);

    private AtomicInteger count = new AtomicInteger();
    private long startTimestamp;
    private long endTimestamp;
    private long throughputMax = 0;

    public void reset() {
        count = new AtomicInteger();
        startTimestamp = 0;
        endTimestamp = 0;
        throughputMax = 0;
    }

    public void inc() {
        init();
        endTimestamp = System.currentTimeMillis();
        int currentCount = count.addAndGet(1);

        log.info("...completed (" + currentCount + "). " + getThroughputInfoFor(currentCount));
    }

    private String getThroughputInfoFor(int currentCount) {

        long timeDiff = (endTimestamp - startTimestamp) / 1000;

        if (timeDiff == 0) {
            return "Current throughput (jobs/s ): " + currentCount;
        } else {
            long throughput = currentCount / timeDiff;
            if (throughput > throughputMax) {
                throughputMax = throughput;
            }
            return "Current throughput (jobs/s ): " + throughput + ", Max: " + throughputMax;
        }
    }

    public void init() {
        if (startTimestamp == 0) {
            startTimestamp = System.currentTimeMillis();
        }
    }
}
