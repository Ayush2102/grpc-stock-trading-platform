package com.jain.grpc_stock_trading_server.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Document(collection = "stocks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Stock {
    @Id
    private ObjectId id;

    @Field("stock_symbol")
    private String stockSymbol;

    private double price;

    @Field("last_updated")
    private LocalDateTime lastUpdated;
}
