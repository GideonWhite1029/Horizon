package dev.gideonwhite1029.horizon.util;

import dev.gideonwhite1029.horizon.HorizonConfig;
import net.minecraft.Util;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class AsyncPlayerDataSaving {

    private AsyncPlayerDataSaving() {
    }

    public static void saveAsync(Runnable runnable) {
        if (!HorizonConfig.asyncPlayerDataSaving) {
            runnable.run();
            return;
        }

        ExecutorService ioExecutor = Util.backgroundExecutor().service();
        CompletableFuture.runAsync(runnable, ioExecutor);
    }
}
