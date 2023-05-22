## Clojure Developer. Урок 5

Внешние зависимости и работа с классами Java.

### Зависимости

#### Пакеты из Clojars

Прописываем нужные пакеты вместе с версиями в [project.clj](project.clj). Те пакеты, которые нужны в процессе выполнения программы, добавляются в корневой набор `:dependencies`. То, что нужно только во время разработки или, скажем, тестирования, прописывается в нужный профиль.

#### Локальные копии

Здесь вы можете найти пример "локальной копии внешней библиотеки": [checkouts/example](checkouts/example).

**Внимание:** перед началом работы с кодом этого проекта вам нужно зайти в [checkouts/example](checkouts/example) и выполнить там `lein install`. Эта команда установит пакет в локальный реестр пакетов Maven (обычно он находится в `~/.m2/`). После выполнения этой команды пакет будет доступен для "установки" в текущий проект — lein сможет его подключить.

### Исключения

Обрабатываются ошибки с помощью [try](https://clojuredocs.org/clojure.core/try)/[catch](https://clojuredocs.org/clojure.core/catch)/[finally](https://clojuredocs.org/clojure.core/finally). Кидаются с помощью [throw](https://clojuredocs.org/clojure.core/throw).

```clojure
(try
  ;; тут возникает ошибка
  (catch Exception e
    ;; делаем что-то с экземпляром ошибки
    )
  (finally
    ;; делаем обязательную завершающую работу
    )
)
```

### Классы Java

Подробнее читаем тут: [Java interop](https://clojure.org/reference/java_interop).

Любой класс перед использованием нужно импортировать. Это делается с помощью функции [import](https://clojuredocs.org/clojure.core/import) или с помощью соответствующей опции макроса [ns](https://clojuredocs.org/clojure.core/ns).
