package com.durganmcbroom.resources;

import java.util.concurrent.*;

public class UseTimeout {
    private static final ExecutorService executor;
    static {
        executor = Executors.newCachedThreadPool();
    }

    public static <T> T orNull(
            long timeout,
            Callable<T> block
    ) throws Throwable {
        final var future = executor.submit(block);

        try {
            return future.get(timeout, TimeUnit.MILLISECONDS);
        } catch (TimeoutException ignored) {
            return null;
        } catch (ExecutionException e) {
            throw e.getCause();
        }
    }
}
