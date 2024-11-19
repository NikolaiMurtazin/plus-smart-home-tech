    DROP TABLE IF EXISTS products CASCADE;

    CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

    CREATE TABLE IF NOT EXISTS products
    (
        product_id       UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
        product_name     VARCHAR(255)     NOT NULL,
        description      TEXT             NOT NULL,
        image_src        VARCHAR(255),
        quantity_state   VARCHAR(50)      NOT NULL,
        product_state    VARCHAR(50)      NOT NULL,
        rating           DOUBLE PRECISION NOT NULL,
        product_category VARCHAR(50)      NOT NULL,
        price            NUMERIC(19, 2)   NOT NULL
    );