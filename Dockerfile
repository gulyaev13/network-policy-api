FROM openjdk:8-jdk-alpine
RUN addgroup -S network_policy && adduser -S network_policy -G network_policy

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
RUN chown network_policy:network_policy /app.jar

USER network_policy:network_policy
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]