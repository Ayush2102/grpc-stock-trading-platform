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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final OrderRepository orderRepository;
    private final PortfolioRepository portfolioRepository;

    @Transactional
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
        if ("EXECUTED".equals(order.getStatus()) || "REJECTED".equals(order.getStatus())) {
            log.warn("Order {} already finalized with status {}", order.getOrderId(), order.getStatus());
            return;
        }

        // --- Load Portfolio ---
        Portfolio portfolio = portfolioRepository.findAll()
                .stream()
                .findFirst()
                .orElse(
                        Portfolio.builder()
                                .holdings(new HashMap<>())
                                .lastUpdated(LocalDateTime.now())
                                .build()
                );


        int currentHolding =
                portfolio.getHoldings().getOrDefault(order.getStockSymbol(), 0);

        try {
            // --- Update Portfolio ---
            if ("BUY".equals(order.getSide())) {

                portfolio.getHoldings().put(
                        order.getStockSymbol(),
                        currentHolding + order.getQuantity()
                );

            } else if ("SELL".equals(order.getSide())) {

                if (currentHolding < order.getQuantity()) {
                    log.warn(
                            "Rejecting SELL order {}: insufficient holdings (have={}, want={})",
                            order.getOrderId(),
                            currentHolding,
                            order.getQuantity()
                    );

                    order.setStatus("REJECTED");
                    orderRepository.save(order);
                    return;
                }

                portfolio.getHoldings().put(
                        order.getStockSymbol(),
                        currentHolding - order.getQuantity()
                );
            }

            portfolio.setLastUpdated(LocalDateTime.now());
            portfolioRepository.save(portfolio);

            order.setStatus("EXECUTED");
            orderRepository.save(order);

            log.info("Order {} executed successfully", order.getOrderId());

        } catch (Exception ex) {
            log.error("Order execution failed for {}", order.getOrderId(), ex);

            order.setStatus("REJECTED");
            orderRepository.save(order);

            throw ex;
        }
    }
}
