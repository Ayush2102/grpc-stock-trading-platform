package com.jain.trading.client.controller;

import com.jain.trading.client.model.Order;
import com.jain.trading.client.model.PlaceOrderInput;
import com.jain.trading.client.service.StockGraphQLService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class StockMutationResolver {

    private final StockGraphQLService stockService;

    @MutationMapping
    public Order placeOrder(@Argument PlaceOrderInput input) {

        if (input == null) {
            log.warn("Invalid placeOrder input: null");
            throw new IllegalArgumentException("Order input must not be null");
        }

        if (input.getOrderId() == null || input.getOrderId().trim().isEmpty()) {
            log.warn("Invalid orderId received: [{}]", input.getOrderId());
            throw new IllegalArgumentException("orderId must not be blank");
        }

        log.info("GraphQL mutation: placeOrder(orderId={})", input.getOrderId());
        return stockService.placeOrder(input);
    }
}
