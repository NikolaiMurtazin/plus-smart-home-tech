DROP TABLE IF EXISTS addresses CASCADE;
DROP TABLE IF EXISTS deliveries CASCADE;

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE addresses (
    address_id  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    country     VARCHAR(100)    NOT NULL,
    city        VARCHAR(100)    NOT NULL,
    street      VARCHAR(255)    NOT NULL,
    house       VARCHAR(5)      NOT NULL,
    flat        VARCHAR(5)
);

CREATE TABLE deliveries (
    delivery_id     UUID PRIMARY KEY,
    from_address_id UUID        NOT NULL,
    to_address_id   UUID        NOT NULL,
    order_id        UUID        NOT NULL,
    delivery_state  VARCHAR(20) NOT NULL,
    delivery_weight DOUBLE      NOT NULL,
    delivery_volume DOUBLE      NOT NULL,
    fragile         BOOLEAN     NOT NULL,
    FOREIGN KEY (from_address_id) REFERENCES addresses (address_id),
    FOREIGN KEY (to_address_id) REFERENCES addresses (address_id)
);