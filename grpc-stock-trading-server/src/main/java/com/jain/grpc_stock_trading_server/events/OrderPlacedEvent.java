package com.jain.grpc_stock_trading_server.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderPlacedEvent {

    private String orderId;
    private String stockSymbol;
    private String side; // BUY / SELL
    private int quantity;
    private Instant createdAt;
}
