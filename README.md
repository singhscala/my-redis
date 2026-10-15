# My Redis — A Redis-Inspired Key-Value Server in Java

A lightweight, Redis-inspired in-memory key-value server built from scratch using **Java 21**. The project implements a TCP server, RESP-style request and response handling, string and hash data structures, key expiration, concurrent client handling, and structured error handling.

The goal is to understand how a networked data server works internally, from receiving a client request to executing a command and returning a protocol-encoded response.

## Features

- **TCP Server:** Accepts client connections over TCP sockets.
- **RESP Protocol:** Parses array-based requests and encodes responses using Redis Serialization Protocol (RESP)-style framing.
- **String Operations:** Supports storing, retrieving, checking, and deleting string keys.
- **Hash Operations:** Supports hash fields and common hash-related commands.
- **TTL Expiration:** Supports expiring string keys after a specified duration.
- **Concurrent Clients:** Uses a fixed-size thread pool to handle multiple client connections.
- **Key Type Safety:** Prevents string operations on hash keys and hash operations on string keys.
- **Error Handling:** Handles invalid commands, incorrect argument counts, invalid TTL values, key-type mismatches, and protocol errors.
- **JUnit Testing:** Includes automated tests for core functionality and error scenarios.
- **Maven Build:** Uses Maven to compile, test, and package the application into an executable JAR.

## Tech Stack

| Technology | Purpose |
|---|---|
| Java 21 | Core implementation |
| TCP Sockets | Client-server communication |
| RESP-style protocol | Request parsing and response encoding |
| ConcurrentHashMap | Concurrent in-memory storage |
| ExecutorService | Concurrent client handling |
| Maven | Build and dependency management |
| JUnit 5 | Automated testing |

## Supported Commands

### String Commands

| Command | Description | Example |
|---|---|---|
| `SET` | Store a string value | `SET name Prachi` |
| `GET` | Retrieve a string value | `GET name` |
| `DEL` | Delete a key | `DEL name` |
| `EXISTS` | Check whether a key exists | `EXISTS name` |
| `TYPE` | Return the key's data type | `TYPE name` |

### Expiration

| Command | Description | Example |
|---|---|---|
| `SET ... EX` | Store a string with a TTL in seconds | `SET session abc123 EX 60` |

Keys with an expiration time are removed when their TTL expires. The expiration worker periodically checks stored string keys for expiration.

### Hash Commands

| Command | Description |
|---|---|
| `HSET` | Set a field in a hash |
| `HGET` | Retrieve a hash field |
| `HGETALL` | Retrieve all fields and values |
| `HDEL` | Delete a hash field |
| `HEXISTS` | Check whether a hash field exists |
| `HLEN` | Count fields in a hash |
| `HKEYS` | Retrieve all field names |
| `HVALS` | Retrieve all field values |
| `HMGET` | Retrieve multiple hash fields |

## Architecture

The application is organized into components responsible for different parts of the request lifecycle.

1. **RedisClient:** Reads commands from the terminal and sends them to the server.
2. **RespEncoder:** Encodes client commands into protocol-formatted requests.
3. **RedisServer:** Accepts TCP connections and assigns client handling tasks to a thread pool.
4. **RespParser:** Reads and parses incoming requests.
5. **CommandHandler:** Validates commands and arguments, executes supported operations, and converts errors into responses.
6. **RedisStore:** Manages string and hash data in memory, including key-type checks and expiration-related operations.
7. **RespEncoder:** Encodes server responses for transmission back to the client.
8. **ExpirationWorker:** Periodically checks for expired string keys.

### Request Flow

```text
Terminal Client
      |
      | TCP connection
      v
   RedisServer
      |
      v
   RespParser
      |
      v
  CommandHandler
      |
      v
   RedisStore
      |
      v
  RedisResponse
      |
      v
   RespEncoder
      |
      v
 Terminal Client
```

## Getting Started

### Prerequisites

Install the following:

- Java Development Kit (JDK) 21
- Apache Maven
- Git

Verify your environment:

```bash
java -version
mvn -version
git --version
```

### Clone the Repository

```bash
git clone https://github.com/singhscala/my-redis.git
cd my-redis
```

### Run the Tests

```bash
mvn clean test
```

Maven compiles the project and executes the automated tests.

### Package the Application

```bash
mvn clean package
```

This generates the application JAR in the `target/` directory.

### Start the Server

```bash
java -jar target/my-redis-1.0-SNAPSHOT.jar
```

Expected startup output:

```text
My Redis server started on port 6379
```

The server reads the `PORT` environment variable if configured; otherwise, it defaults to port `6379`.

### Start the Client

Open a second terminal in the project directory:

```bash
java -cp target/my-redis-1.0-SNAPSHOT.jar com.prachi.client.RedisClient
```

The client connects to `localhost:6379` by default. Its host and port can be configured using `REDIS_HOST` and `REDIS_PORT`.

## Example Usage

Start the server first, then launch the client in a separate terminal.

```text
redis> SET project my-redis
OK

redis> GET project
my-redis

redis> TYPE project
string

redis> EXISTS project
1

redis> HSET user:1 name Prachi
1

redis> HGET user:1 name
Prachi

redis> HLEN user:1
1

redis> TYPE user:1
hash

redis> DEL project
1

redis> GET project
(nil)
```

The exact formatting of responses depends on the response type and client parser.

### Example: Expiring a Key

```text
redis> SET session abc123 EX 10
OK

redis> GET session
abc123
```

After the TTL expires, retrieving the key returns a null response. The exact timing depends on the expiration mechanism.

## Key Engineering Concepts

This project explores several important backend engineering concepts:

- **TCP socket programming:** Establishing connections and exchanging data through input and output streams.
- **Protocol design:** Framing requests and responses so the server can interpret commands reliably.
- **Concurrent programming:** Handling multiple clients with an `ExecutorService` and coordinating access to shared data.
- **In-memory data structures:** Representing strings and hashes using concurrent maps.
- **Time-based expiration:** Tracking TTLs and removing expired entries.
- **Command validation:** Checking command names, argument counts, TTL values, and data types.
- **Separation of concerns:** Keeping networking, protocol handling, command execution, storage, and response encoding in separate components.
- **Automated testing:** Using JUnit to verify supported operations and expected error conditions.

## Current Limitations

This project is intended for learning and experimentation. It is not a production-ready Redis replacement.

- **In-memory storage only:** Data is lost when the server stops or restarts.
- **No persistence:** RDB snapshots, append-only files, and recovery are not implemented.
- **No authentication or authorization:** The server does not implement client authentication.
- **No TLS:** Communication is not encrypted by the application protocol itself.
- **Limited Redis compatibility:** Only the documented commands and supported protocol behavior are implemented.
- **Fixed-size thread pool:** Client handling uses a configured, fixed-size pool rather than a production-grade connection-management system.
- **No replication or clustering:** The server operates as a standalone instance.
- **Not intended for untrusted networks:** Do not expose the server directly to the public internet.

## Future Improvements

Potential improvements include:

- Add persistence and restart recovery.
- Expand RESP parsing and protocol compatibility.
- Add stronger protocol validation and resource limits.
- Improve TTL handling and expiration tests.
- Add authentication and secure transport.
- Add benchmarks and load tests.
- Add Docker support and deployment automation.
- Improve observability through structured logging and metrics.

## Author

**Prachi Singh**

- GitHub: [@singhscala](https://github.com/singhscala)

---

*Built as a hands-on project to explore Java networking, concurrent programming, protocol parsing, and the internal design of a Redis-inspired data server.*