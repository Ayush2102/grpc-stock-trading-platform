package com.jain.grpc_stock_trading_server.config;

import io.grpc.*;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Counter;

public class GrpcMetricsInterceptor implements ServerInterceptor {

    private final MeterRegistry meterRegistry;

    public GrpcMetricsInterceptor(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        String methodName = call.getMethodDescriptor().getFullMethodName();

        Timer.Sample sample = Timer.start(meterRegistry);
        Counter counter = meterRegistry.counter("grpc_server_calls_total", "method", methodName);

        ServerCall.Listener<ReqT> listener = next.startCall(call, headers);

        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(listener) {
            @Override
            public void onComplete() {
                sample.stop(meterRegistry.timer("grpc_server_duration_seconds", "method", methodName));
                counter.increment();
                super.onComplete();
            }

            @Override
            public void onCancel() {
                sample.stop(meterRegistry.timer("grpc_server_duration_seconds", "method", methodName));
                super.onCancel();
            }
        };
    }
}
