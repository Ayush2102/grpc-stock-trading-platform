package com.jain.trading.client.controller;

import com.jain.trading.client.model.Stock;
import com.jain.trading.client.service.StockGraphQLService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class StockQueryResolver {

    private final StockGraphQLService stockService;

    @QueryMapping
    public Stock getStock(@Argument String symbol) {
        if (symbol == null || symbol.trim().isEmpty()) {
            log.warn("Invalid stock symbol received: [{}]", symbol);
            throw new IllegalArgumentException("Stock symbol must not be blank");
        }

        log.info("GraphQL query: getStock(symbol={})", symbol);
        return stockService.getStock(symbol);
    }
}
