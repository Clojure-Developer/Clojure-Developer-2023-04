CREATE TABLE "account" (
    "id" INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    "username" VARCHAR (255) UNIQUE NOT NULL,
    "password" VARCHAR (255) NOT NULL,
    "email" VARCHAR (255) UNIQUE NOT NULL,
    "created_on" TIMESTAMP NOT NULL
);
--;;
INSERT INTO "account"(
        "username",
        "password",
        "email",
        "created_on"
    )
VALUES (
        'admin',
        'password123',
        'admin@otus.ru',
        NOW()
    ),
    (
        'nikita',
        'password',
        'nikita@otus.ru',
        NOW()
    );
