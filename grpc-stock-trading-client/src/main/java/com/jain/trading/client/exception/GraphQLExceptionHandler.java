package com.jain.trading.client.exception;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GraphQLExceptionHandler extends DataFetcherExceptionResolverAdapter {
    @Override
    protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {
        log.error("GraphQL Error: {}", ex.getMessage(), ex);

        if (ex instanceof StockNotFoundException) {
            return GraphqlErrorBuilder.newError(env)
                    .message(ex.getMessage())
                    .errorType(graphql.ErrorType.DataFetchingException)
                    .build();
        } else if (ex instanceof IllegalArgumentException) {
            return GraphqlErrorBuilder.newError(env)
                    .message("Invalid input: " + ex.getMessage())
                    .errorType(graphql.ErrorType.ValidationError)
                    .build();
        }

        // fallback for unexpected errors
        return GraphqlErrorBuilder.newError(env)
                .message("Internal server error")
                .errorType(graphql.ErrorType.DataFetchingException)
                .build();
    }
}
