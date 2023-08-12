# url-shortener

## Установка и запуск

1. `lein deps` установит зависимости для серверной части
2. `lein with-profile cljs deps` установит зависимости для ShadowCLJS
3. `npx shadow-cljs compile app` скомпилирует JS для браузера
4. `lein run` запустит сервер на http://localhost:8000
