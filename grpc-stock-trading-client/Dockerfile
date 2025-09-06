FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn -pl grpc-stock-trading-client -am clean package -DskipTests

FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY --from=build /app/grpc-stock-trading-client/target/*.jar app.jar
# main app REST/gRPC endpoints
EXPOSE 8080
# actuator metrics for Prometheus
#EXPOSE 9401
ENTRYPOINT ["java", "-jar", "app.jar"]
