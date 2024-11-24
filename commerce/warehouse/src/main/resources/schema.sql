DROP TABLE IF EXISTS bookings CASCADE;
DROP TABLE IF EXISTS booking_products CASCADE;
DROP TABLE IF EXISTS warehouse_products CASCADE;

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE bookings
(
    booking_id       UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    shopping_cart_id UUID             NOT NULL,
    delivery_weight  DOUBLE PRECISION NOT NULL,
    delivery_volume  DOUBLE PRECISION NOT NULL,
    fragile          BOOLEAN          NOT NULL,
);

CREATE TABLE booking_products
(
    booking_id UUID    NOT NULL,
    product_id UUID    NOT NULL,
    quantity   INTEGER NOT NULL,
    PRIMARY KEY (booking_id, product_id),
    FOREIGN KEY (booking_id) REFERENCES bookings (booking_id) ON DELETE CASCADE
);

CREATE TABLE warehouse_products
(
    product_id         UUID PRIMARY KEY,
    fragile            BOOLEAN          NOT NULL,
    weight             DOUBLE PRECISION NOT NULL,
    quantity_available INTEGER          NOT NULL,
    width              DOUBLE PRECISION NOT NULL,
    height             DOUBLE PRECISION NOT NULL,
    depth              DOUBLE PRECISION NOT NULL
);