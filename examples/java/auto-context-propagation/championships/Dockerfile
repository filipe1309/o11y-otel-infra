FROM maven:3.8.6-amazoncorretto-18 as build

COPY pom.xml .
COPY src src
RUN --mount=type=cache,target=/root/.m2 mvn -B package

FROM amazoncorretto:18.0.2

ENV CHAMPIONSHIPS_JSON_PATH=championships.json
ENV OTEL_AGENT_VERSION=1.21.0

ADD championships.json .
COPY --from=build target/championships-1.0.0.jar .
ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v${OTEL_AGENT_VERSION}/opentelemetry-javaagent.jar opentelemetry-javaagent.jar
EXPOSE 8082 9092

CMD ["java", "-jar", "championships-1.0.0.jar"]
