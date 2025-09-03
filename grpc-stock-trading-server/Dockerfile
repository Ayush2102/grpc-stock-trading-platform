FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# copy the whole repo so Maven sees all 3 modules
COPY . .

# build server (this will also build common-protos first)
RUN mvn -pl grpc-stock-trading-server -am clean package -DskipTests

FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY --from=build /app/grpc-stock-trading-server/target/*.jar app.jar
EXPOSE 9090
ENTRYPOINT ["java", "-jar", "app.jar"]
