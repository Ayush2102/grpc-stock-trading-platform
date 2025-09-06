package com.jain.grpc_stock_trading_server.config;

import io.grpc.ServerInterceptor;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.grpc.MetricCollectingServerInterceptor;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcMetricsConfig {

    @GrpcGlobalServerInterceptor
    @Bean
    public ServerInterceptor globalGrpcMetricsInterceptor(MeterRegistry meterRegistry) {
        // This interceptor will be applied to all gRPC services automatically
        return new MetricCollectingServerInterceptor(meterRegistry);
    }
}
