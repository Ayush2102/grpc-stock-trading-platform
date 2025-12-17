package com.jain.trading.client.service;

import com.jain.grpc.*;
import io.grpc.Context;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
public class StockClientService {

    @GrpcClient("stockService")
    private StockTradingServiceGrpc.StockTradingServiceBlockingStub blockingStub;

    @GrpcClient("stockService")
    private StockTradingServiceGrpc.StockTradingServiceStub asyncStub;

    // Holds current cancellable context if a subscription is active
    private final AtomicReference<Context.CancellableContext> activeSubscription = new AtomicReference<>();

    /**
     * Unary call: fetch current price once.
     */
    public StockResponse getStockPrice(String stockSymbol) {
        StockRequest request = StockRequest.newBuilder()
                .setStockSymbol(stockSymbol)
                .build();

        log.info("Requesting price for stock: {}", stockSymbol);

        StockResponse response = blockingStub.getStockPrice(request);

        log.info("Received price for {}: {}", response.getStockSymbol(), response.getPrice());
        return response;
    }

    /**
     * Server streaming call: subscribe to live updates.
     * User explicitly starts this subscription.
     */
    public void startSubscription(String stockSymbol) {
        if (activeSubscription.get() != null) {
            log.warn("A subscription is already active. Cancel it before starting a new one.");
            return;
        }

        StockRequest request = StockRequest.newBuilder()
                .setStockSymbol(stockSymbol)
                .build();

        // Create a cancellable context so client can stop manually
        Context.CancellableContext cancellableContext = Context.current().withCancellation();
        activeSubscription.set(cancellableContext);

        cancellableContext.run(() -> {
            log.info("Starting subscription for stock: {}", stockSymbol);

            asyncStub.subscribeStockPrice(request, new StreamObserver<>() {
                @Override
                public void onNext(StockResponse response) {
                    log.info("Live update -> {}: {}", response.getStockSymbol(), response.getPrice());
                }

                @Override
                public void onError(Throwable t) {
                    log.error("Subscription error: {}", t.getMessage());
                    activeSubscription.set(null);
                }

                @Override
                public void onCompleted() {
                    log.info("Subscription completed by server (cutoff reached or stream ended).");
                    activeSubscription.set(null);
                }
            });
        });
    }

    /**
     * Cancel the active subscription explicitly by user.
     */
    public void stopSubscription() {
        Context.CancellableContext context = activeSubscription.getAndSet(null);
        if (context != null) {
            log.info("Cancelling active subscription...");
            context.cancel(null);
        } else {
            log.warn("No active subscription found to cancel.");
        }
    }

    public PlaceOrderResponse placeOrder(PlaceOrderRequest request) {
        log.info("Placing order via gRPC: {}", request.getOrderId());

        PlaceOrderResponse response = blockingStub.placeOrder(request);

        log.info(
                "Received PlaceOrder response for {} with status {}",
                response.getOrderId(),
                response.getStatus()
        );

        return response;
    }

    public GetOrderResponse getOrder(String orderId) {
        GetOrderRequest request = GetOrderRequest.newBuilder()
                .setOrderId(orderId)
                .build();

        log.info("Requesting order details for orderId: {}", orderId);

        GetOrderResponse response = blockingStub.getOrder(request);

        log.info(
                "Received order {} with status {}",
                response.getOrderId(),
                response.getStatus()
        );

        return response;
    }

    public GetPortfolioResponse getPortfolio() {
        GetPortfolioRequest request = GetPortfolioRequest.newBuilder().build();

        log.info("Requesting portfolio via gRPC");

        GetPortfolioResponse response = blockingStub.getPortfolio(request);

        log.info("Received portfolio with {} holdings", response.getHoldingsCount());
        return response;
    }

}
