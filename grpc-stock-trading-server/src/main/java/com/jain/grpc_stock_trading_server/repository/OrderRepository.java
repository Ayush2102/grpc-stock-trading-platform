package com.jain.grpc_stock_trading_server.repository;

import com.jain.grpc_stock_trading_server.entity.Order;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OrderRepository extends MongoRepository<Order, ObjectId> {
    Order findByOrderId(String orderId);
}
