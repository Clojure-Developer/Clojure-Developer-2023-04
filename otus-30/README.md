# otus-30


### Конфигурация для сборки в project.clj

```clojure
:main ^:skip-aot otus-30.core

{:uberjar {:aot :all}}
```
Почитать про компиляцию
https://clojure.org/reference/compilation


### Сборка jar файла

```shell
lein uberjar
```
### Запуск 

```shell
java -jar target/production-app.jar
```


### Docker

Сборка Docker образа

```shell
docker build --tag otus-clojure/app:1.1 .
```
Запуск Docker контейнера

```shell
docker run -p 8080:8080 otus-clojure/app
```


### JVM options

https://blogs.oracle.com/javamagazine/post/the-best-hotspot-jvm-options-and-switches-for-java-11-through-java-17

- -XX:InitialRAMPercentage
- -XX:MaxRAMPercentage
- -XX:+UseSerialGC
- -XX:+UseParallelGC
- -XX:+UseZGC
- -XX:+UnlockExperimentalVMOptions
- -XX:+UseContainerSupport


### Логирование

Java logging frameworks
https://lambdaisland.com/blog/2020-06-12-logging-in-clojure-making-sense-of-the-mess

Pure Clojure logging
https://github.com/BrunoBonacci/mulog


### Сбор JMX метрик

https://github.com/prometheus/jmx_exporter
https://opentelemetry.io/docs/collector
https://prometheus.io


### Сборка проекта с помощью GraalVM

https://github.com/graalvm/graalvm-ce-builds/releases
https://github.com/clj-easy/graalvm-clojure/tree/master/

```shell
lein do clean, uberjar

native-image --report-unsupported-elements-at-runtime \
             --initialize-at-build-time \
             --no-server \
             -jar ./target/production-app.jar \
             -H:Name=./target/hello-world
             
./target/hello-world
```

```shell
time java -jar ./target/production-app.jar

time ./target/hello-world
```


### AWS Lambda

Required Java interface
https://github.com/aws/aws-lambda-java-libs/blob/main/aws-lambda-java-runtime-interface-client/README.md



## Homework

Применить принципы hexagonal architecture в проекте на выбор (Pokemon app)

### Задание

- Разделить код проекта на доменные модули
- Выделить в сервисы код взаимодействующий с внешними системами
- Подключить к проекту фреймворк Duct
- Написать конфигурации для двух режимов запуска приложения (dev, production)
- Применить dependency injection для тестирования логики приложения
- Настроить production сборку для проекта
  - Создать Dockerfile для сборки и запуска
  - Создать entrypoint.sh фаил
  - Настроить логирование и сборку метрик (JMX)
