package com.jain.trading.client.service;

import com.jain.grpc.StockRequest;
import com.jain.grpc.StockResponse;
import com.jain.grpc.StockTradingServiceGrpc;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

@Service
public class StockClientService {

    @GrpcClient("stockService")
    private StockTradingServiceGrpc.StockTradingServiceBlockingStub serviceBlockingStub;

    public StockResponse getStockPrice(String stockSymbol){
        StockRequest request = StockRequest.newBuilder().setStockSymbol(stockSymbol).build();

        return serviceBlockingStub.getStockPrice(request);
    }
}
