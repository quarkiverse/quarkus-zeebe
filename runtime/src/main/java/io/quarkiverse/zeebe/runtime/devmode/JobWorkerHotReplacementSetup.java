package io.quarkiverse.zeebe.runtime.devmode;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.quarkus.dev.spi.HotReplacementContext;
import io.quarkus.dev.spi.HotReplacementSetup;

public class JobWorkerHotReplacementSetup implements HotReplacementSetup {

    private HotReplacementContext context;
    private static final long TWO_SECONDS = 2000;

    private volatile long nextUpdate;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    public void setupHotDeployment(HotReplacementContext context) {
        this.context = context;
        JobWorkerReplacementInterceptor.onMessage(new OnMessage());
    }

    @Override
    public void close() {
        executor.shutdown();
    }

    private class OnMessage implements Runnable {

        @Override
        public void run() {
            if (nextUpdate < System.currentTimeMillis()) {
                synchronized (this) {
                    if (nextUpdate < System.currentTimeMillis()) {
                        executor.execute(() -> {
                            try {
                                context.doScan(true);
                            } catch (RuntimeException e) {
                                throw e;
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        });
                        // we update at most once every 2s
                        nextUpdate = System.currentTimeMillis() + TWO_SECONDS;
                    }
                }
            }
        }
    }

}
