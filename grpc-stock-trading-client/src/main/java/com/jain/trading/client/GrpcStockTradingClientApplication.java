package com.jain.trading.client;

import com.jain.trading.client.service.StockClientService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GrpcStockTradingClientApplication implements CommandLineRunner {

    private final StockClientService stockClientService;

    public GrpcStockTradingClientApplication(StockClientService stockClientService) {
        this.stockClientService = stockClientService;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("gRPC client: " + stockClientService.getStockPrice("GOOGL"));
    }

    public static void main(String[] args) {
		SpringApplication.run(GrpcStockTradingClientApplication.class, args);
	}

}
