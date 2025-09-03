# gRPC Stock Trading Platform

A multi-module Java project using **Spring Boot** and **gRPC** to simulate a stock trading platform.  
It demonstrates clean module separation, shared protobuf contracts, and containerized deployment.

## Modules

1. **common-protos** – Contains the shared `.proto` files for server and client communication.
2. **grpc-stock-trading-server** – Backend microservice that handles stock trading operations (gRPC server, MongoDB persistence).
3. **grpc-stock-trading-client** – Client app that interacts with the server using gRPC and runs a demo gRPC call at startup

## Features

- gRPC-based communication between server and client
- Spring Boot framework
- MongoDB integration (server-side)
- Protobuf for data serialization
- Multi-module Maven project structure
- Dockerized setup with docker-compose
- Environment-based configuration for secrets

## Tech Stack
- Java 17 · Spring Boot 3 · gRPC · Protobuf
- MongoDB · Maven (multi-module) · Docker & Docker Compose

## Run Locally
1. Create env file:
    - `.env.local` → `MONGO_USER`, `MONGO_PASS`
2. In IntelliJ, add these envs to **Run Configurations**.
3. Run:
   ```bash
   ./mvnw -pl grpc-stock-trading-server spring-boot:run
   ./mvnw -pl grpc-stock-trading-client spring-boot:run
    ```
## Run with Docker
1. Create env file:
    - `.env.docker` → `MONGO_USER`, `MONGO_PASS`
3. Run:
   ```bash
   docker compose up --build
    ```
- Server exposed at localhost:9091
- Client exposed at localhost:8080

---
