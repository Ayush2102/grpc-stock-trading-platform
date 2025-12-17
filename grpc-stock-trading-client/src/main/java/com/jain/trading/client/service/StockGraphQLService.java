package com.jain.trading.client.service;

import com.jain.grpc.*;
import com.jain.trading.client.exception.OrderNotFoundException;
import com.jain.trading.client.exception.PortfolioNotFoundException;
import com.jain.trading.client.exception.StockNotFoundException;
import com.jain.trading.client.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockGraphQLService {

    private final StockClientService stockClientService;

    public Stock getStock(String symbol) {
        log.debug("Fetching stock [{}] via gRPC", symbol);

        try {
            StockResponse response = stockClientService.getStockPrice(symbol);

            if (response == null || response.getStockSymbol().isEmpty()) {
                throw new StockNotFoundException("Stock Symbol not found: " + symbol);
            }

            return Stock.builder()
                    .symbol(response.getStockSymbol())
                    .price(response.getPrice())
                    .timestamp(response.getTimestamp())
                    .build();

        } catch (Exception ex) {
            log.error("Error fetching stock [{}]: {}", symbol, ex.getMessage(), ex);
            throw ex;
        }
    }

    public Order getOrder(String orderId) {
        log.debug("Fetching order [{}] via gRPC", orderId);

        try {
            GetOrderResponse response = stockClientService.getOrder(orderId);

            if (response == null || response.getOrderId().isBlank()) {
                throw new OrderNotFoundException("Order not found: " + orderId);
            }

            return Order.builder()
                    .orderId(response.getOrderId())
                    .symbol(response.getStockSymbol())
                    .side(response.getSide().name())
                    .quantity(response.getQuantity())
                    .status(response.getStatus().name())
                    .createdAt(response.getCreatedAt())
                    .build();

        } catch (Exception ex) {
            log.error("Error fetching order [{}]: {}", orderId, ex.getMessage(), ex);
            throw ex;
        }
    }

    public Order placeOrder(PlaceOrderInput input) {
        log.debug("Placing order [{}] via gRPC", input != null ? input.getOrderId() : null);

        try {
            validatePlaceOrder(input);

            PlaceOrderRequest grpcRequest =
                    PlaceOrderRequest.newBuilder()
                            .setOrderId(input.getOrderId())
                            .setStockSymbol(input.getSymbol())
                            .setSide(OrderSide.valueOf(input.getSide()))
                            .setQuantity(input.getQuantity())
                            .build();

            PlaceOrderResponse response =
                    stockClientService.placeOrder(grpcRequest);

            if (response == null || response.getOrderId().isBlank()) {
                throw new RuntimeException("Invalid response received from order service");
            }

            return Order.builder()
                    .orderId(response.getOrderId())
                    .status(response.getStatus().name())
                    .build();

        } catch (Exception ex) {
            log.error(
                    "Error placing order [{}]: {}",
                    input != null ? input.getOrderId() : null,
                    ex.getMessage(),
                    ex
            );
            throw ex;
        }
    }

    public Portfolio getPortfolio() {
        log.debug("Fetching portfolio via gRPC");

        try {
            GetPortfolioResponse response = stockClientService.getPortfolio();

            if (response == null) {
                throw new PortfolioNotFoundException("Portfolio not found");
            }

            List<PortfolioHolding> holdings =
                    response.getHoldingsList().stream()
                            .map(h ->
                                    PortfolioHolding.builder()
                                            .symbol(h.getStockSymbol())
                                            .quantity(h.getQuantity())
                                            .build()
                            )
                            .toList();

            return Portfolio.builder()
                    .holdings(holdings)
                    .lastUpdated(response.getLastUpdated())
                    .build();

        } catch (Exception ex) {
            log.error("Error fetching portfolio: {}", ex.getMessage(), ex);
            throw ex;
        }
    }

    private void validatePlaceOrder(PlaceOrderInput input) {
        if (input == null) {
            throw new IllegalArgumentException("order input must not be null");
        }
        if (input.getOrderId() == null || input.getOrderId().isBlank()) {
            throw new IllegalArgumentException("orderId is required");
        }
        if (input.getSymbol() == null || input.getSymbol().isBlank()) {
            throw new IllegalArgumentException("symbol is required");
        }
        if (input.getQuantity() <= 0) {
            throw new IllegalArgumentException("quantity must be > 0");
        }
    }
}
