package com.jain.grpc_stock_trading_server.kafka;

import com.jain.grpc_stock_trading_server.entity.Order;
import com.jain.grpc_stock_trading_server.entity.Portfolio;
import com.jain.grpc_stock_trading_server.events.OrderPlacedEvent;
import com.jain.grpc_stock_trading_server.repository.OrderRepository;
import com.jain.grpc_stock_trading_server.repository.PortfolioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final OrderRepository orderRepository;
    private final PortfolioRepository portfolioRepository;

    @KafkaListener(
            topics = "order-placed",
            groupId = "order-execution-group"
    )
    public void consume(OrderPlacedEvent event) {

        log.info("Received OrderPlacedEvent for orderId={}", event.getOrderId());

        // --- Fetch order ---
        Order order = orderRepository.findByOrderId(event.getOrderId());
        if (order == null) {
            log.error("Order not found for orderId={}, skipping execution", event.getOrderId());
            return;
        }

        // --- Idempotency check ---
        if ("EXECUTED".equals(order.getStatus())) {
            log.warn("Order {} already executed, skipping", order.getOrderId());
            return;
        }

        try {
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

            // --- Mark order EXECUTED ---
            order.setStatus("EXECUTED");
            orderRepository.save(order);

            log.info("Order {} executed successfully", order.getOrderId());

        } catch (Exception ex) {
            log.error(
                    "Failed to execute order {}. Will retry on next consumption.",
                    order.getOrderId(),
                    ex
            );
            throw ex;
        }
    }
}
