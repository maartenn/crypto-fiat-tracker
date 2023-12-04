FROM eclipse-temurin:21-jdk-alpine AS build

COPY . .

RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine

EXPOSE 8080

COPY --from=build /build/libs/crypto-fiat-tracker-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]