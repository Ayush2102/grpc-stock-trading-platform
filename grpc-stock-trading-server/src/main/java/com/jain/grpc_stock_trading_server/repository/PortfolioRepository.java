package com.jain.grpc_stock_trading_server.repository;

import com.jain.grpc_stock_trading_server.entity.Portfolio;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PortfolioRepository extends MongoRepository<Portfolio, ObjectId> {
}
