CREATE TABLE "account_role" (
    "account_id" INT NOT NULL,
    "role_id" INT NOT NULL,
    "grant_date" TIMESTAMP,
    PRIMARY KEY ("account_id", "role_id"),
    FOREIGN KEY ("role_id") REFERENCES "role" ("id"),
    FOREIGN KEY ("account_id") REFERENCES "account" ("id")
);
--;;
INSERT INTO "account_role"("account_id", "role_id", "grant_date")
VALUES (
        (
            SELECT "id"
            FROM "account"
            WHERE "username" = 'admin'
        ),
        (
            SELECT "id"
            FROM "role"
            WHERE "name" = 'admin'
        ),
        NOW()
    ),
    (
        (
            SELECT "id"
            FROM "account"
            WHERE "username" = 'nikita'
        ),
        (
            SELECT "id"
            FROM "role"
            WHERE "name" = 'user'
        ),
        NOW()
    );