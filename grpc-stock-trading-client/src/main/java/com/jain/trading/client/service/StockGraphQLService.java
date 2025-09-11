package com.jain.trading.client.service;

import com.jain.grpc.StockResponse;
import com.jain.trading.client.exception.StockNotFoundException;
import com.jain.trading.client.model.Stock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockGraphQLService {

    private final StockClientService stockClientService;

    public Stock getStock(String symbol){
        log.debug("Fetching stock [{}] via gRPC", symbol);

        try {
            StockResponse grpcResponse = stockClientService.getStockPrice(symbol);

            if(grpcResponse == null || grpcResponse.getStockSymbol().isEmpty()) {
                throw new StockNotFoundException("Stock Symbol not found: " + symbol);
            }

            return Stock.builder()
                    .symbol(grpcResponse.getStockSymbol())
                    .price(grpcResponse.getPrice())
                    .timestamp(grpcResponse.getTimestamp())
                    .build();

        } catch (Exception ex) {
            log.error("Error fetching stock [{}]: {}", symbol, ex.getMessage(), ex);
            throw ex;
        }
    }
}
