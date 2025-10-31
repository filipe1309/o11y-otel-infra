FROM maven:3.8.6-amazoncorretto-18 as build

COPY pom.xml .
COPY src src
RUN --mount=type=cache,target=/root/.m2 mvn -B package

FROM amazoncorretto:18.0.2

ENV TEAMS_JSON_PATH=teams.json
ENV OTEL_AGENT_VERSION=1.21.0

ADD teams.json .
COPY --from=build target/teams-1.0.0.jar .
ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v${OTEL_AGENT_VERSION}/opentelemetry-javaagent.jar opentelemetry-javaagent.jar
EXPOSE 8084 9094

CMD ["java", "-jar", "teams-1.0.0.jar"]
