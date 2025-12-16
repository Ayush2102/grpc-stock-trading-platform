package com.jain.grpc_stock_trading_server.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    private ObjectId id;

    private String orderId;
    private String stockSymbol;

    private String side; // BUY / SELL
    private int quantity;

    private String status; // ACCEPTED / EXECUTED / REJECTED

    private LocalDateTime createdAt;
}
