package com.jain.grpc_stock_trading_server.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Document(collection = "portfolio")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Portfolio {
    @Id
    private ObjectId id;

    private Map<String, Integer> holdings;

    private LocalDateTime lastUpdated;
}
