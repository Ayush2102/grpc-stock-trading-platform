package com.jain.grpc_stock_trading_server.repository;

import com.jain.grpc_stock_trading_server.entity.Stock;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface StockRepository extends MongoRepository<Stock, ObjectId> {
    Stock findByStockSymbol(String stockSymbol);
}
