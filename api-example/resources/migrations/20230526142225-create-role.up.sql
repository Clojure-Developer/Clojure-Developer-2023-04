CREATE TABLE "role"(
    "id" INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    "name" VARCHAR (255) UNIQUE NOT NULL
);
--;;
INSERT INTO "role"("name")
VALUES ('admin'),
    ('user');