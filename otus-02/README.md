## Clojure Developer. Урок 2

### Leiningen

https://leiningen.org/

Установка

MacOS
```shell
brew install leiningen
```

Debian
```shell
sudo apt update
sudo apt install leiningen
```

SDK-MAN
https://sdkman.io/sdks#leiningen

```shell
lein -v
# Leiningen 2.9.8 on Java 11.0.2 OpenJDK 64-Bit Server VM
```

---

Создаём новый проект

```shell
lein new my-project
```

Можно использовать шаблоны https://clj-templates.com/

```shell
lein new compojure-app my-project
```

Главный файл [project.clj](project.clj)

https://codeberg.org/leiningen/leiningen/src/branch/stable/sample.project.clj

---

Основные команды

```shell
lein repl
```

```shell
lein run
```

```shell
lein trampoline run -m otus-02.server 5000
```

```shell
lein test
```

```shell
lein uberjar
```


### ДЗ

1. Написать функцию определяющую является ли строка палиндромом
   (строка, которая читается одинаково справа налево и с лева направо)
   алгоритм не должен учитывать знаки препинания и заглавные буквы
2. Написать функцию определяющую является ли строка палиндромом
   (строка которая содержит все символы алфавита хотя бы по одному разу, например `The quick brown fox jumps over the lazy dog`)
3. Реализовать алгоритм для поиска самой длинной строки, которая является общим дочерним элементом входных строк (*, опционально)
4. Реализовать классический метод составления секретных сообщений, называемый `quare code` (*, опционально)

Для проверки используйте тесты в папке `test/otus_02/homework`
