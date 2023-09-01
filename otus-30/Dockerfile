FROM clojure:temurin-20-lein as builder

RUN mkdir /build

WORKDIR /build

COPY src src
COPY project.clj project.clj
COPY entrypoint.sh entrypoint.sh

RUN lein uberjar



FROM eclipse-temurin:20-jdk

RUN mkdir -p /opt/jmx_exporter
RUN wget -O /opt/jmx_exporter/jmx_prometheus_javaagent-0.16.1.jar https://repo1.maven.org/maven2/io/prometheus/jmx/jmx_prometheus_javaagent/0.16.1/jmx_prometheus_javaagent-0.16.1.jar
COPY jmx-config.yaml /opt/jmx_exporter/config.yaml

RUN mkdir -p /opt/elastic-apm
RUN wget -O /opt/elastic-apm/elastic-apm-agent-1.34.1.jar https://search.maven.org/remotecontent?filepath=co/elastic/apm/elastic-apm-agent/1.34.1/elastic-apm-agent-1.34.1.jar
COPY elastic.properties /opt/elastic-apm/elastic.properties

RUN mkdir /service

COPY --from=builder /build/target/production-app.jar /service/production-app.jar
COPY --from=builder /build/entrypoint.sh /service/entrypoint.sh

EXPOSE 8080

ENTRYPOINT ["./service/entrypoint.sh"]
