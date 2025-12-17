package com.jain.trading.client.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Portfolio {
    private List<PortfolioHolding> holdings;
    private String lastUpdated;
}
