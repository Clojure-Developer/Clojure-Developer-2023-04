### Запуск сервера

```shell
lein repl
```

```clojure
(-main)
```

Сервер запустится на 6379 порту

redis-cli устанавливается вместе с самим redis https://redis.io/docs/getting-started/installation/ 

Запуск клиента 

```shell
redis-cli
```

Поддерживаемые команды

```shell
echo hello

ping

set my-key some-value

set my-key some-value px 1200

get my-key
```
