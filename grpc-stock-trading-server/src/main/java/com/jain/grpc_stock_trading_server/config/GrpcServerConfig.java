package com.jain.grpc_stock_trading_server.config;

import io.micrometer.core.instrument.MeterRegistry;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcServerConfig {

    @Bean
    @GrpcGlobalServerInterceptor
    public GrpcMetricsInterceptor grpcMetricsInterceptor(MeterRegistry meterRegistry) {
        return new GrpcMetricsInterceptor(meterRegistry);
    }
}
