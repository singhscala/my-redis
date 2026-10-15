# My Redis — A Redis-Inspired Server in Java

A lightweight, in-memory key-value server built from scratch using Java 21. The project implements TCP communication and the Redis Serialization Protocol (RESP), with support for string and hash data types.

## Features

- TCP server supporting multiple client connections
- RESP request parsing and response encoding
- String commands: `SET`, `GET`, `DEL`, `EXISTS`
- Hash commands: `HSET`, `HGET`, `HGETALL`, `HDEL`, `HEXISTS`, `HLEN`, `HKEYS`, `HVALS`, `HMGET`
- Key inspection using `TYPE`
- Key expiration with `SET ... EX`
- Background expiration worker
- Key-type validation to prevent string/hash conflicts
- Custom error handling
- Concurrent client handling with an executor service
- JUnit 5 tests

## Tech Stack

- Java 21
- Maven
- TCP sockets
- Redis Serialization Protocol (RESP)
- JUnit 5
- Concurrent collections and multithreading

## Getting Started

### Prerequisites

- JDK 21
- Maven

### Build and Test

```bash
mvn clean test
```

### Start the Server

Run `com.prachi.server.RedisServer` from your IDE, or use the packaged application once a runnable JAR has been configured.

The server defaults to port `6379`. Set the `PORT` environment variable to use a different port.

### Start the Client

Run `com.prachi.client.RedisClient` in a separate terminal or IDE run configuration.

Enter commands at the `redis>` prompt. Type `exit` to disconnect.

## Example

```text
redis> SET name Prachi
OK

redis> GET name
Prachi

redis> TYPE name
string

redis> HSET user:1 name Prachi
1

redis> HGET user:1 name
Prachi

redis> TYPE user:1
hash
```

## Architecture

1. **RedisServer** — accepts TCP connections and dispatches client handling.
2. **RespParser** — parses incoming RESP requests.
3. **CommandHandler** — validates and executes supported commands.
4. **RedisStore** — stores strings and hashes in memory.
5. **RespEncoder** — serializes responses using RESP.
6. **ExpirationWorker** — periodically checks for expired keys.

## Limitations

- Data is held in memory and is lost when the server stops.
- This is a learning project inspired by Redis, not a production Redis replacement.
- Authentication, persistence, replication, and production-grade security are not implemented.

## Future Improvements

- Additional Redis-compatible commands
- Persistent storage
- Improved protocol and concurrency tests
- Secure remote access for demonstrations
