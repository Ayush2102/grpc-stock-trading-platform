package com.jain.trading.client.controller;

import com.jain.trading.client.model.Order;
import com.jain.trading.client.model.Portfolio;
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

    @QueryMapping
    public Order getOrder(@Argument String orderId) {
        if (orderId == null || orderId.trim().isEmpty()) {
            log.warn("Invalid orderId received: [{}]", orderId);
            throw new IllegalArgumentException("OrderId must not be blank");
        }

        log.info("GraphQL query: getOrder(orderId={})", orderId);
        return stockService.getOrder(orderId);
    }

    @QueryMapping
    public Portfolio getPortfolio() {
        log.info("GraphQL query: getPortfolio");
        return stockService.getPortfolio();
    }
}
