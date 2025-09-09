package com.jain.trading.client;

import com.jain.trading.client.service.StockClientService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GrpcStockTradingClientApplication {

    private final StockClientService stockClientService;

    public GrpcStockTradingClientApplication(StockClientService stockClientService) {
        this.stockClientService = stockClientService;
    }

//    @Override
//    public void run(String... args) throws Exception {
//        stockClientService.startSubscription("GOOGL");
//        Thread.sleep(7000);
//        stockClientService.stopSubscription();
//    }

    public static void main(String[] args) {
		SpringApplication.run(GrpcStockTradingClientApplication.class, args);
	}

//    @PostConstruct
//    public void startGrpcSubscription() {
//        // Start subscription when app starts
//        stockClientService.startSubscription("GOOGL");
//    }
//
//    @PreDestroy
//    public void stopGrpcSubscription() {
//        // Stop subscription gracefully on shutdown
//        stockClientService.stopSubscription();
//    }
}
