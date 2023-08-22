# url-shortener

## Установка и запуск

1. `lein deps` установит зависимости для серверной части
2. `lein with-profile cljs deps` установит зависимости для ShadowCLJS
3. `npx shadow-cljs compile app` скомпилирует JS для браузера
4. `lein run` запустит сервер на http://localhost:8000

## Запуск в режиме разработки

Запускаем проект в режиме REPL
```
lein repl
```

Затем из репла загружаем окружение разработки
```
user=> (dev)
:loaded
dev=>
```

Теперь можно запустить систему следующими образом
```
dev=> (go)
Server started on port: 8000
:initiated
```

После того как поменяли код, приложение можно обновить с помощью
```
dev=> (reset)
:reloading (...)
:resumed
```
