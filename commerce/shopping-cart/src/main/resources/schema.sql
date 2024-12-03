DROP TABLE IF EXISTS shopping_carts CASCADE;
DROP TABLE IF EXISTS shopping_cart_items CASCADE;

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE shopping_carts
(
    shopping_cart_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    username         VARCHAR(255) NOT NULL,
    active           BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE shopping_cart_items
(
    shopping_cart_id UUID    NOT NULL,
    product_id       UUID    NOT NULL,
    quantity         INTEGER NOT NULL,
    PRIMARY KEY (shopping_cart_id, product_id),
    FOREIGN KEY (shopping_cart_id) REFERENCES shopping_carts (shopping_cart_id) ON DELETE CASCADE
);