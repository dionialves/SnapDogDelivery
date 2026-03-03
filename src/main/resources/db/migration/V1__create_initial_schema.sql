-- =========================================================
-- V1 — Schema inicial completo do SnapDog Delivery
-- =========================================================

CREATE TABLE tb_users (
    id         BIGSERIAL    PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    email      VARCHAR(100) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    role       VARCHAR(20)  NOT NULL,
    created_at TIMESTAMP    NOT NULL,
    updated_at TIMESTAMP
);

CREATE TABLE tb_customers (
    id           BIGSERIAL    PRIMARY KEY,
    name         VARCHAR(100) NOT NULL,
    phone        VARCHAR(20)  NOT NULL,
    email        VARCHAR(100) NOT NULL,
    city         VARCHAR(50)  NOT NULL,
    state        VARCHAR(2)   NOT NULL,
    neighborhood VARCHAR(100) NOT NULL,
    street       VARCHAR(100) NOT NULL,
    zip_code     VARCHAR(9)   NOT NULL,
    number       VARCHAR(10)  NOT NULL,
    complement   VARCHAR(100),
    password     VARCHAR(255) NOT NULL,
    active       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at   DATE         NOT NULL
);

CREATE TABLE tb_products (
    id          BIGSERIAL      PRIMARY KEY,
    name        VARCHAR(100)   NOT NULL,
    price       NUMERIC(10, 2) NOT NULL,
    description VARCHAR(500),
    image_url   VARCHAR(500),
    active      BOOLEAN        NOT NULL DEFAULT TRUE,
    featured    BOOLEAN        NOT NULL DEFAULT FALSE,
    category    VARCHAR(20)
);

CREATE TABLE tb_company_settings (
    id            BIGINT       PRIMARY KEY,
    company_name  VARCHAR(100) NOT NULL,
    email         VARCHAR(100) NOT NULL,
    phone         VARCHAR(20)  NOT NULL,
    address       VARCHAR(300) NOT NULL,
    opening_hours VARCHAR(200) NOT NULL,
    copyright     VARCHAR(100) NOT NULL
);

CREATE TABLE tb_orders (
    id               BIGSERIAL    PRIMARY KEY,
    customer_id      BIGINT       NOT NULL REFERENCES tb_customers(id),
    status           VARCHAR(30)  NOT NULL DEFAULT 'PENDING',
    origin           VARCHAR(10)  NOT NULL DEFAULT 'MANUAL',
    created_at       TIMESTAMP    NOT NULL,
    delivery_address VARCHAR(500)
);

CREATE TABLE tb_product_orders (
    id            BIGSERIAL      PRIMARY KEY,
    product_id    BIGINT         NOT NULL REFERENCES tb_products(id),
    order_id      BIGINT         NOT NULL REFERENCES tb_orders(id),
    quantity      INTEGER        NOT NULL,
    price_at_time NUMERIC(10, 2) NOT NULL
);
