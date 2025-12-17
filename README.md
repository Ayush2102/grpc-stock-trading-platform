# gRPC + GraphQL Stock Trading Platform

A multi-module Java project using **Spring Boot**, **gRPC** and **GraphQL** to simulate a stock trading platform.  
It demonstrates clean module separation, shared protobuf contracts, and containerized deployment.

**Design Scope:**
This project models the core trading workflow and intentionally omits user identity and authentication to keep the focus on distributed order processing and consistency.

The system provides:
- **gRPC APIs** for efficient service-to-service communication
- **GraphQL API** layer on the client side for flexible, developer-friendly queries
## Modules

1. **common-protos** – Contains the shared `.proto` files for server and client communication.
2. **grpc-stock-trading-server** – Backend microservice that handles stock trading operations (gRPC server, MongoDB persistence).
3. **grpc-stock-trading-client** – Client app that interacts with the server using gRPC and runs a demo gRPC call at startup

## Features

- gRPC-based communication between server and client
- GraphQL API layer (client-side) for querying stock data
- Order placement and inspection via gRPC command APIs
- Portfolio state tracking on the server
- Spring Boot framework with modular design
- MongoDB integration (server-side)
- Protobuf for strongly-typed service contracts
- Multi-module Maven project structure
- Dockerized setup with docker-compose
- Environment-based configuration for secrets
- Monitoring with Prometheus and Grafana
    - Prometheus collects server and client metrics
    - Grafana visualizes metrics with dashboards

## Tech Stack
- Java 17 · Spring Boot 3 · gRPC · GraphQL · Protobuf
- MongoDB · Maven (multi-module) · Docker & Docker Compose
- Prometheus · Grafana · Micrometer (metrics collection)

## gRPC APIs (Server)

The server exposes the following gRPC APIs, acting as the system of record.

### Stock APIs

- ```GetStockPrice``` – Fetch current stock price

- ```SubscribeStockPrice``` – Stream live stock price updates

### Trading APIs

- ```PlaceOrder``` – Place a BUY/SELL order for a stock

- ```GetOrder``` – Retrieve the status and details of an order

- ```GetPortfolio``` – Retrieve current portfolio holdings


## GraphQL API (Client)

The client module exposes a GraphQL endpoint that acts as a user-facing API layer over the gRPC services.

### Available APIs

**Queries**
- `getStock` – Fetch current stock price
- `getOrder` – Retrieve order details
- `getPortfolio` – Retrieve current portfolio holdings

**Mutations**
- `placeOrder` – Place a BUY/SELL order for a stock

### Endpoint
```POST http://localhost:8085/graphql```
### Example Query
```graphql
query {
  getStock(symbol: "AAPL") {
    symbol
    price
    timestamp
  }
}
```
### Example Response
```graphql
{
  "data": {
    "getStock": {
      "symbol": "AAPL",
      "price": 182.36,
      "timestamp": "2025-09-11T18:45:00Z"
    }
  }
}
```

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
2. Run:
   ```bash
   docker compose up --build
    ```

- Server exposed at localhost:9091 (gRPC)
- Client exposed at localhost:8085
- Prometheus dashboard at localhost:9095
- Grafana dashboard at localhost:3000 (default user: admin / password: admin)

---