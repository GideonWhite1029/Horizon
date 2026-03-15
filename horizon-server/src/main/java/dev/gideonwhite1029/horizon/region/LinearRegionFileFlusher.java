package dev.gideonwhite1029.horizon.region;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

public class LinearRegionFileFlusher implements Runnable {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final LinearRegionFileFlusher INSTANCE = new LinearRegionFileFlusher();

    private final Set<HorizonRegionFile> inManagement = new HashSet<>();
    private ScheduledFuture<?> flusherChecker;
    private ExecutorService ioWorkerPool;

    private LinearRegionFileFlusher() {}

    private synchronized void startIfNeeded() {
        if (this.flusherChecker != null) return;

        int ioThreads = dev.gideonwhite1029.horizon.HorizonConfig.linearIoThreadCount;
        long checkIntervalMs = dev.gideonwhite1029.horizon.HorizonConfig.linearIoFlushDelayMs;

        this.ioWorkerPool = Executors.newFixedThreadPool(
            Math.max(1, ioThreads),
            new ThreadFactoryBuilder()
                .setNameFormat("Linear-IO-Worker-%d")
                .setDaemon(true)
                .build()
        );

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(
            new ThreadFactoryBuilder()
                .setNameFormat("Linear-Flusher-Checker")
                .setDaemon(true)
                .build()
        );
        this.flusherChecker = scheduler.scheduleWithFixedDelay(
            this, checkIntervalMs, checkIntervalMs, TimeUnit.MILLISECONDS
        );
    }

    public void shutdown() {
        if (this.flusherChecker != null) {
            this.flusherChecker.cancel(false);
        }
        if (this.ioWorkerPool != null) {
            this.ioWorkerPool.shutdown();
            try {
                if (!this.ioWorkerPool.awaitTermination(30, TimeUnit.SECONDS)) {
                    LOGGER.warn("Linear I/O worker pool did not terminate in 30s, forcing shutdown");
                    this.ioWorkerPool.shutdownNow();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                this.ioWorkerPool.shutdownNow();
            }
        }
    }

    @Override
    public void run() {
        final HorizonRegionFile[] copied;
        synchronized (this) {
            copied = this.inManagement.toArray(new HorizonRegionFile[0]);
        }

        final List<HorizonRegionFile> toRemove = new ArrayList<>();
        for (HorizonRegionFile file : copied) {
            if (file.isClosedVolatile()) {
                toRemove.add(file);
                continue;
            }

            if (!file.isMarkedToSave()) continue;

            if (!file.tryMarkFlushing()) continue;

            this.ioWorkerPool.execute(() -> {
                try {
                    file.syncIfNeeded();
                } catch (IOException e) {
                    LOGGER.error("Failed to sync linear region file {}: ", file.getRegionFile(), e);
                }
            });
        }

        if (!toRemove.isEmpty()) {
            synchronized (this) {
                toRemove.forEach(this.inManagement::remove);
            }
        }
    }

    public synchronized void addFile(HorizonRegionFile file) {
        startIfNeeded();
        this.inManagement.add(file);
    }

    public synchronized void removeFile(HorizonRegionFile file) {
        this.inManagement.remove(file);
    }
}
