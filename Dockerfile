#FROM eclipse-temurin:21-jdk-alpine AS build
#
#COPY . .
#
#RUN ./gradlew bootJar --no-daemon
#
#
#
#EXPOSE 8080
#
#COPY --from=build /build/libs/crypto-fiat-tracker-0.0.1-SNAPSHOT.jar app.jar
#
#ENTRYPOINT ["java", "-jar", "app.jar"]

# not best practice to run jdk but otherwise I get many timeouts during deployment, probably because it has to retrieve JDK and JRE ...

FROM eclipse-temurin:21-jdk-alpine AS build
COPY . .
RUN ./gradlew bootJar
FROM eclipse-temurin:21-jdk-alpine
EXPOSE 8080
COPY --from=build /build/libs/crypto-fiat-tracker-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]

