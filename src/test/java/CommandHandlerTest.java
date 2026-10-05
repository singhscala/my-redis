import com.prachi.command.CommandHandler;
import com.prachi.store.RedisStore;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CommandHandlerTest {

    @Test
    void shouldSetAndGetValue() {

        RedisStore store = new RedisStore();
        CommandHandler handler = new CommandHandler(store);

        assertEquals(
                "Ok",
                handler.handle("SET name Prachi")
        );

        assertEquals(
                "Prachi",
                handler.handle("GET name")
        );
    }

    @Test
    void shouldReturnNilForMissingKey() {

        RedisStore store = new RedisStore();
        CommandHandler handler = new CommandHandler(store);

        assertEquals(
                "(nil)",
                handler.handle("GET name")
        );
    }

    @Test
    void shouldDeleteKey() {

        RedisStore store = new RedisStore();
        CommandHandler handler = new CommandHandler(store);

        handler.handle("SET name Prachi");

        assertEquals(
                "1",
                handler.handle("DEL name")
        );

        assertEquals(
                "(nil)",
                handler.handle("GET name")
        );
    }

    @Test
    void shouldCheckKeyExistence() {

        RedisStore store = new RedisStore();
        CommandHandler handler = new CommandHandler(store);

        assertEquals(
                "0",
                handler.handle("EXISTS name")
        );

        handler.handle("SET name Prachi");

        assertEquals(
                "1",
                handler.handle("EXISTS name")
        );
    }

    @Test
    void shouldExpireKey() throws InterruptedException {

        RedisStore store = new RedisStore();
        CommandHandler handler = new CommandHandler(store);

        assertEquals(
                "OK",
                handler.handle("SET session abc123 EX 1")
        );

        assertEquals(
                "abc123",
                handler.handle("GET session")
        );

        Thread.sleep(1100);

        assertEquals(
                "(nil)",
                handler.handle("GET session")
        );
    }
}
