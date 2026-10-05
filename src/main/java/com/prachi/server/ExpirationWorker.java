package com.prachi.server;

import com.prachi.store.RedisStore;

public class ExpirationWorker implements Runnable {

    private final RedisStore store;

    public ExpirationWorker(RedisStore store) {
        this.store = store;
    }

    @Override
    public void run() {

        while (!Thread.currentThread().isInterrupted()) {

            store.removeExpiredKeys();

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}