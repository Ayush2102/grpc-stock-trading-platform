package com.jain.trading.client.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    private String orderId;
    private String symbol;
    private String side;       // BUY / SELL
    private Integer quantity;
    private String status;     // ACCEPTED / EXECUTED / REJECTED
    private String createdAt;
}
