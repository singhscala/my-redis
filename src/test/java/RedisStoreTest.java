import com.prachi.exception.RedisErrorCode;
import com.prachi.exception.RedisException;
import com.prachi.store.RedisStore;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

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


    @Test
    void shouldReturnOneForNewFieldAndZeroForUpdate() {
        RedisStore store = new RedisStore();

        assertEquals(1, store.hset("user", "name", "Prachi"));
        assertEquals(0, store.hset("user", "name", "Singh"));

        assertEquals("Singh", store.hget("user", "name"));
    }

    @Test
    void shouldDeleteMultipleHashFields() {
        RedisStore store = new RedisStore();

        store.hset("user", "name", "Prachi");
        store.hset("user", "city", "Noida");

        assertEquals(2, store.hdel("user", "name", "city"));
        assertEquals(0, store.hlen("user"));
        assertFalse(store.exists("user"));
    }

    @Test
    void shouldCheckHashFieldExistence() {
        RedisStore store = new RedisStore();

        store.hset("user", "name", "Prachi");

        assertEquals(1, store.hexists("user", "name"));
        assertEquals(0, store.hexists("user", "city"));
        assertEquals(0, store.hexists("missing", "name"));
    }

    @Test
    void shouldReturnHashKeysAndValues() {
        RedisStore store = new RedisStore();

        store.hset("user", "name", "Prachi");
        store.hset("user", "city", "Noida");

        assertEquals(
                java.util.Set.of("name", "city"),
                new java.util.HashSet<>(store.hkeys("user"))
        );

        assertEquals(
                java.util.Set.of("Prachi", "Noida"),
                new java.util.HashSet<>(store.hvals("user"))
        );
    }

    @Test
    void shouldReturnRequestedHashValuesInOrder() {
        RedisStore store = new RedisStore();

        store.hset("user", "name", "Prachi");
        store.hset("user", "city", "Noida");

        assertEquals(
                Arrays.asList("Noida", null, "Prachi"),
                store.hmget("user", "city", "age", "name")
        );
    }

    @Test
    void shouldRejectHashOperationOnStringKey() {
        RedisStore store = new RedisStore();
        store.set("user:1", "Prachi");

        RedisException exception = assertThrows(
                RedisException.class,
                () -> store.hset("user:1", "name", "Prachi")
        );

        assertEquals(
                RedisErrorCode.KEY_TYPE_MISMATCH,
                exception.getError()
        );
    }

    @Test
    void shouldRejectStringOperationOnHashKey() {
        RedisStore store = new RedisStore();
        store.hset("user:1", "name", "Prachi");

        RedisException exception = assertThrows(
                RedisException.class,
                () -> store.get("user:1")
        );

        assertEquals(
                RedisErrorCode.KEY_TYPE_MISMATCH,
                exception.getError()
        );
    }

    @Test
    void shouldPreventSameKeyFromBeingBothStringAndHash() throws Exception {
        RedisStore store = new RedisStore();
        String key = "concurrent:user";
        CountDownLatch start = new CountDownLatch(1);

        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            Future<?> stringTask = executor.submit(() -> {
                awaitStart(start);
                store.set(key, "Prachi");
            });

            Future<?> hashTask = executor.submit(() -> {
                awaitStart(start);
                store.hset(key, "name", "Prachi");
            });

            start.countDown();

            int successCount = 0;
            int typeMismatchCount = 0;

            for (Future<?> task : List.of(stringTask, hashTask)) {
                try {
                    task.get();
                    successCount++;
                } catch (java.util.concurrent.ExecutionException e) {
                    if (e.getCause() instanceof RedisException) {
                        typeMismatchCount++;
                    } else {
                        throw e;
                    }
                }
            }

            assertEquals(1, successCount);
            assertEquals(1, typeMismatchCount);
        }
    }

    private void awaitStart(CountDownLatch start) {
        try {
            start.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    @Test
    void shouldReturnStringForStringKey() {
        RedisStore store = new RedisStore();

        store.set("name", "Prachi");

        assertEquals("string", store.type("name"));
    }
}
