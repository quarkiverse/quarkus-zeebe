package io.quarkiverse.zeebe;

import java.util.concurrent.ScheduledExecutorService;

public interface ZeebeScheduledExecutorService {

    ScheduledExecutorService scheduledExecutorService();
}
