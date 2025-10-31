FROM maven:3.8.6-amazoncorretto-18 as build

COPY pom.xml .
COPY src src
RUN --mount=type=cache,target=/root/.m2 mvn -B package

FROM amazoncorretto:18.0.2

ENV MATCHES_JSON_PATH=matches.json
ENV RELATIONSHIPS_JSON_PATH=relationships.json
ENV OTEL_AGENT_VERSION=1.21.0

ADD matches.json .
ADD relationships.json .
COPY --from=build target/matches-1.0.0.jar .
ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v${OTEL_AGENT_VERSION}/opentelemetry-javaagent.jar opentelemetry-javaagent.jar
EXPOSE 8083 9093

CMD ["java", "-jar", "matches-1.0.0.jar"]
