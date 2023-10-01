DROP TABLE IF EXISTS accounts CASCADE;

CREATE TABLE accounts
(
    name  VARCHAR(50)                            NOT NULL,
    pin VARCHAR(4)                            NOT NULL,
    account_number BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    money DECIMAL NOT NULL,
    CONSTRAINT pk_account PRIMARY KEY (account_number)
);