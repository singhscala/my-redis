import com.prachi.store.RedisStore;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class RedisStoreTest {

    @Test
    void shouldStoreAndRetrieveValue() {

        RedisStore store = new RedisStore();

        store.set("name", "Prachi");

        assertEquals("Prachi", store.get("name"));
    }

    @Test
    void shouldReturnNullForMissingKey() {

        RedisStore store = new RedisStore();

        assertNull(store.get("name"));
    }

    @Test
    void shouldDeleteValue() {

        RedisStore store = new RedisStore();

        store.set("name", "Prachi");

        store.delete("name");

        assertNull(store.get("name"));
    }

    @Test
    void shouldCheckIfKeyExists() {

        RedisStore store = new RedisStore();

        store.set("name", "Prachi");

        assertTrue(store.exists("name"));
        assertFalse(store.exists("age"));
    }

    @Test
    void shouldHandleConcurrentWrites() throws InterruptedException {

        RedisStore store = new RedisStore();

        int numberOfThreads = 10;
        int operationsPerThread = 100;

        ExecutorService executor =
                Executors.newFixedThreadPool(numberOfThreads);

        for (int i = 0; i < numberOfThreads; i++) {

            int threadNumber = i;

            executor.submit(() -> {

                for (int j = 0; j < operationsPerThread; j++) {

                    String key = "key-" + threadNumber + "-" + j;

                    store.set(key, "value");
                }
            });
        }

        executor.shutdown();

        assertTrue(
                executor.awaitTermination(
                        5,
                        TimeUnit.SECONDS
                )
        );

        for (int i = 0; i < numberOfThreads; i++) {

            for (int j = 0; j < operationsPerThread; j++) {

                String key = "key-" + i + "-" + j;

                assertEquals(
                        "value",
                        store.get(key)
                );
            }
        }
    }

    @Test
    void shouldRemoveExpiredKeys() throws InterruptedException {

        RedisStore store = new RedisStore();

        long expiryTime =
                System.currentTimeMillis() + 1000;

        store.set("session", "abc123", expiryTime);

        assertEquals("abc123", store.get("session"));

        Thread.sleep(1500);

        store.removeExpiredKeys();

        assertNull(store.get("session"));
    }

    @Test
    void shouldStoreHashFields() {
        RedisStore store = new RedisStore();

        store.hset("user:1", "name", "Prachi");
        store.hset("user:1", "city", "Noida");

        assertEquals("Prachi",
                store.hget("user:1", "name"));

        assertEquals("Noida",
                store.hget("user:1", "city"));

        assertNull(store.hget("user:1", "age"));
    }

    @Test
    void shouldRetrieveAllHashFields() {
        RedisStore store = new RedisStore();

        store.hset("user:1", "name", "Prachi");
        store.hset("user:1", "city", "Noida");

        Map<String, String> result = store.hgetAll("user:1");

        assertEquals(2, result.size());
        assertEquals("Prachi", result.get("name"));
        assertEquals("Noida", result.get("city"));
    }

    @Test
    void shouldReturnEmptyMapForMissingHash() {
        RedisStore store = new RedisStore();

        assertTrue(store.hgetAll("unknown").isEmpty());
    }
}
