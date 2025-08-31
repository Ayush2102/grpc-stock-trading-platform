package com.jain.grpc_stock_trading_server.service;

import com.jain.grpc.StockRequest;
import com.jain.grpc.StockResponse;
import com.jain.grpc.StockTradingServiceGrpc;
import com.jain.grpc_stock_trading_server.entity.Stock;
import com.jain.grpc_stock_trading_server.repository.StockRepository;
import io.grpc.Status;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.grpc.server.service.GrpcService;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@GrpcService
public class StockTradingServiceImpl extends StockTradingServiceGrpc.StockTradingServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(StockTradingServiceImpl.class);
    private final StockRepository stockRepository;

    public StockTradingServiceImpl(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    @Override
    public void getStockPrice(StockRequest request, StreamObserver<StockResponse> responseObserver) {
        String stockSymbol = request.getStockSymbol();
        log.info("Received gRPC request for stock symbol: {}", stockSymbol);

        if(stockSymbol.isBlank()){
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

            if(stockEntity == null){
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

}
