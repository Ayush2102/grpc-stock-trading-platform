package com.jain.grpc_stock_trading_server.kafka;

import com.jain.grpc_stock_trading_server.events.OrderPlacedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventProducer {

    private static final String TOPIC = "order-placed";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishOrderPlaced(OrderPlacedEvent event) {
        log.info("Publishing OrderPlacedEvent for orderId={}", event.getOrderId());

        kafkaTemplate.send(TOPIC, event.getOrderId(), event);

        log.info("OrderPlacedEvent published for orderId={}", event.getOrderId());
    }
}
