#!/bin/bash

APP_NAME=otus-app
JAR_PATH=/service/production-app.jar
ENVIRONMENT=dev

# Sets initial values of variables
PWD="$(dirname "$0")"
LOG_DIR="$PWD/log"
EC2_INSTANCE=$(hostname)

export MALLOC_ARENA_MAX=4
# Stop the JVM from being allowed to use up all of
# Docker's virtual memory. Use if it's a problem
# see https://siddhesh.in/posts/malloc-per-thread-arenas-in-glibc.html

set -eu

trap 'error_handler' ERR 1 2 3 4 5 6

error_handler() {
  ERROR_CODE=$?
  echo "App crashed: $ERROR_CODE"
  exit $ERROR_CODE
}

cd $PWD

# JMX prometheus exporter javaagent configuration
JMX_OPTS="-javaagent:/opt/jmx_exporter/jmx_prometheus_javaagent-0.16.1.jar=8080:/opt/jmx_exporter/config.yaml"

# APM Elastic javaagent configuration
APM_OPTS="-javaagent:/opt/elastic-apm/elastic-apm-agent-1.34.1.jar \
     -Delastic.apm.service_name=${APP_NAME} \
     -Delastic.apm.server_urls=${APM_HOST} \
     -Delastic.apm.secret_token=${APM_SECRET_TOKEN} \
     -Delastic.apm.environment=${ENVIRONMENT} \
     -Delastic.apm.config_file=/opt/elastic-apm/elastic.properties"

# JVM options
JAVA_OPTS="-XX:InitialRAMPercentage=30 -XX:MaxRAMPercentage=85 -XX:+UseContainerSupport -XshowSettings:system "

java $APM_OPTS \
  $JMX_OPTS \
  $JAVA_OPTS \
  -jar $JAR_PATH "$@"
