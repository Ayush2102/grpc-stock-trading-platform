package com.jain.grpc_stock_trading_server.service;

import com.jain.grpc.*;
import com.jain.grpc_stock_trading_server.entity.Order;
import com.jain.grpc_stock_trading_server.entity.Portfolio;
import com.jain.grpc_stock_trading_server.entity.Stock;
import com.jain.grpc_stock_trading_server.repository.OrderRepository;
import com.jain.grpc_stock_trading_server.repository.PortfolioRepository;
import com.jain.grpc_stock_trading_server.repository.StockRepository;
import io.grpc.Status;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@GrpcService
public class StockTradingServiceImpl extends StockTradingServiceGrpc.StockTradingServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(StockTradingServiceImpl.class);
    private final StockRepository stockRepository;
    private final PortfolioRepository portfolioRepository;
    private final OrderRepository orderRepository;

    public StockTradingServiceImpl(StockRepository stockRepository, PortfolioRepository portfolioRepository, OrderRepository orderRepository) {
        this.stockRepository = stockRepository;
        this.portfolioRepository = portfolioRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    public void getStockPrice(StockRequest request, StreamObserver<StockResponse> responseObserver) {
        String stockSymbol = request.getStockSymbol();
        log.info("Received gRPC request for stock symbol: {}", stockSymbol);

        if (stockSymbol.isBlank()) {
            log.warn("Received invalid stock symbol: '{}'", stockSymbol);
            responseObserver.onError(
                    Status.INVALID_ARGUMENT
                            .withDescription("Stock symbol cannot be null")
                            .asRuntimeException()
            );
            return;
        }
        try {
            Stock stockEntity = stockRepository.findByStockSymbol(stockSymbol);

            if (stockEntity == null) {
                log.warn("Stock not found: {}", stockSymbol);
                responseObserver.onError(
                        Status.NOT_FOUND
                                .withDescription("Stock not found: " + stockSymbol)
                                .asRuntimeException()
                );
                return;
            }

            StockResponse stockResponse = StockResponse.newBuilder()
                    .setStockSymbol(stockEntity.getStockSymbol())
                    .setPrice(stockEntity.getPrice())
                    .setTimestamp(stockEntity.getLastUpdated().toString())
                    .build();

            log.info("Returning price {} for stock {}", stockEntity.getPrice(), stockSymbol);

            responseObserver.onNext(stockResponse);
            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("Error fetching stock price for {}: {}", stockSymbol, e.getMessage(), e);
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Internal server error")
                            .withCause(e)
                            .asRuntimeException()
            );
        }

    }

    @Override
    public void subscribeStockPrice(StockRequest request, StreamObserver<StockResponse> responseObserver) {
        String stockSymbol = request.getStockSymbol();
        log.info("Client subscribed to stock price stream for {}", stockSymbol);

        if (stockSymbol.isBlank()) {
            log.warn("Invalid subscription request, empty symbol");
            responseObserver.onError(
                    Status.INVALID_ARGUMENT
                            .withDescription("Stock symbol cannot be empty")
                            .asRuntimeException()
            );
            return;
        }

        Stock stockEntity = stockRepository.findByStockSymbol(stockSymbol);
        if (stockEntity == null) {
            log.warn("Stock not found for subscription: {}", stockSymbol);
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription("Stock not found: " + stockSymbol)
                            .asRuntimeException()
            );
            return;
        }

        final ServerCallStreamObserver<StockResponse> serverObserver =
                (ServerCallStreamObserver<StockResponse>) responseObserver;

        // Scheduler for periodic streaming
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        // Stop streaming after 30 seconds (safety cutoff)
        final long maxDurationSeconds = 30;
        final long startTime = System.currentTimeMillis();

        Runnable task = () -> {
            if (serverObserver.isCancelled()) {
                log.info("Client cancelled subscription for {}", stockSymbol);
                scheduler.shutdown();
                return;
            }

            long elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000;
            if (elapsedSeconds >= maxDurationSeconds) {
                log.info("Max streaming time ({}) reached for {}", maxDurationSeconds, stockSymbol);
                responseObserver.onCompleted();
                scheduler.shutdown();
                return;
            }

            try {
                Stock latest = stockRepository.findByStockSymbol(stockSymbol);
                if (latest == null) {
                    responseObserver.onError(
                            Status.NOT_FOUND
                                    .withDescription("Stock no longer exists: " + stockSymbol)
                                    .asRuntimeException()
                    );
                    scheduler.shutdown();
                    return;
                }

                StockResponse response = StockResponse.newBuilder()
                        .setStockSymbol(latest.getStockSymbol())
                        .setPrice(latest.getPrice())
                        .setTimestamp(latest.getLastUpdated().toString())
                        .build();

                responseObserver.onNext(response);
                log.debug("Pushed update {} for {}", latest.getPrice(), stockSymbol);

            } catch (Exception e) {
                log.error("Error streaming stock {}: {}", stockSymbol, e.getMessage(), e);
                responseObserver.onError(
                        Status.INTERNAL.withDescription("Error streaming stock").withCause(e).asRuntimeException()
                );
                scheduler.shutdown();
            }
        };

        // Push updates every 2 seconds
        scheduler.scheduleAtFixedRate(task, 0, 2, TimeUnit.SECONDS);
    }

    @Override
    public void placeOrder(PlaceOrderRequest request,
                           StreamObserver<PlaceOrderResponse> responseObserver) {

        log.info("Placing order {}", request.getOrderId());

        // --- Validation ---
        if (request.getOrderId().isBlank()
                || request.getStockSymbol().isBlank()
                || request.getQuantity() <= 0) {

            responseObserver.onError(
                    Status.INVALID_ARGUMENT
                            .withDescription("Invalid order request")
                            .asRuntimeException()
            );
            return;
        }

        Stock stock = stockRepository.findByStockSymbol(request.getStockSymbol());
        if (stock == null) {
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription("Stock not found")
                            .asRuntimeException()
            );
            return;
        }

        Order existingOrder = orderRepository.findByOrderId(request.getOrderId());
        if (existingOrder != null) {
            responseObserver.onError(
                    Status.ALREADY_EXISTS
                            .withDescription("Order with this orderId already exists")
                            .asRuntimeException()
            );
            return;
        }

        try {
            // --- Persist Order ---
            Order order = Order.builder()
                    .orderId(request.getOrderId())
                    .stockSymbol(request.getStockSymbol())
                    .side(request.getSide().name())
                    .quantity(request.getQuantity())
                    .status("EXECUTED") // synchronous execution for now
                    .createdAt(LocalDateTime.now())
                    .build();

            orderRepository.save(order);

            // --- Update Portfolio ---
            Portfolio portfolio = portfolioRepository.findAll()
                    .stream()
                    .findFirst()
                    .orElse(
                            Portfolio.builder()
                                    .holdings(new HashMap<>())
                                    .lastUpdated(LocalDateTime.now())
                                    .build()
                    );

            portfolio.getHoldings()
                    .merge(order.getStockSymbol(),
                            order.getQuantity(),
                            Integer::sum);

            portfolio.setLastUpdated(LocalDateTime.now());
            portfolioRepository.save(portfolio);

            responseObserver.onNext(
                    PlaceOrderResponse.newBuilder()
                            .setOrderId(order.getOrderId())
                            .setStatus(OrderStatus.EXECUTED)
                            .setMessage("Order executed successfully")
                            .build()
            );
            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("Order placement failed", e);
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Order processing failed")
                            .asRuntimeException()
            );
        }
    }


    @Override
    public void getOrder(GetOrderRequest request,
                         StreamObserver<GetOrderResponse> responseObserver) {

        if (request.getOrderId().isBlank()) {
            responseObserver.onError(
                    Status.INVALID_ARGUMENT
                            .withDescription("Order ID required")
                            .asRuntimeException()
            );
            return;
        }

        Order order = orderRepository.findByOrderId(request.getOrderId());
        if (order == null) {
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription("Order not found")
                            .asRuntimeException()
            );
            return;
        }

        responseObserver.onNext(
                GetOrderResponse.newBuilder()
                        .setOrderId(order.getOrderId())
                        .setStockSymbol(order.getStockSymbol())
                        .setSide(OrderSide.valueOf(order.getSide()))
                        .setQuantity(order.getQuantity())
                        .setStatus(OrderStatus.valueOf(order.getStatus()))
                        .setCreatedAt(order.getCreatedAt().toString())
                        .build()
        );
        responseObserver.onCompleted();
    }

    @Override
    public void getPortfolio(GetPortfolioRequest request,
                             StreamObserver<GetPortfolioResponse> responseObserver) {

        Portfolio portfolio = portfolioRepository.findAll()
                .stream()
                .findFirst()
                .orElse(null);

        if (portfolio == null) {
            responseObserver.onNext(
                    GetPortfolioResponse.newBuilder()
                            .setLastUpdated(LocalDateTime.now().toString())
                            .build()
            );
            responseObserver.onCompleted();
            return;
        }

        GetPortfolioResponse.Builder builder =
                GetPortfolioResponse.newBuilder()
                        .setLastUpdated(portfolio.getLastUpdated().toString());

        portfolio.getHoldings().forEach((symbol, qty) ->
                builder.addHoldings(
                        Holding.newBuilder()
                                .setStockSymbol(symbol)
                                .setQuantity(qty)
                                .build()
                )
        );

        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }


}
