# otus-27

# Работа с реляционными БД

## JDBC — Java Database Connectivity

Используются

- [next.jdbc](https://cljdoc.org/d/com.github.seancorfield/next.jdbc/)
- драйвер для вашей СУБД. [H2](https://www.h2database.com/) поставляется сразу с [драйвером](https://mavenlibs.com/maven/dependency/com.h2database/h2).

## Генерация SQL

- [HoneySQL](https://github.com/seancorfield/honeysql) генерирует запросы

## Прочее

- [Migratus](https://github.com/yogthos/migratus) применяет миграции к БД, чтобы та имела актуальную схему

## Домашнее задание

В проекте, работающем с PokeAPI реализуйте кэширование запросов к API в виде таблицы в PostgreSQL, где ключом выступит URL, а значением строка с JSON, который вы получили из API.

Опционально можете разложить данные разных сущностей в отдельные таблицы, чтобы не разбирать JSON раз за разом. Как минимум, стоит это сделать для "справочных" данных вроде типов покемонов.
