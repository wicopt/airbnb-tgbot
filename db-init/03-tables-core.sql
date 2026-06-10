-- 03-tables-core.sql
CREATE SCHEMA IF NOT EXISTS core;

-- Таблица room
CREATE TABLE core.room (
    room_number    VARCHAR(255) NOT NULL,
    group_id     VARCHAR(255) NOT NULL,
    building_name  VARCHAR(255) NOT NULL,
    message_name   VARCHAR(255) NOT NULL,
    purchase_price NUMERIC(10,2) NOT NULL,
    purchase_date  DATE NOT NULL,
    CONSTRAINT room_pkey PRIMARY KEY (room_number)
);
INSERT INTO core.room (
    room_number, 
    group_id, 
    building_name, 
    message_name, 
    purchase_price, 
    purchase_date
) VALUES 
    ('2605', 'my-group', 'MBC Condotel', '2605, Орхидея', 69889.94, '2025-02-20'),
    ('2606', 'my-group', 'MBC Condotel', '2606, С черным потолком', 69889.94, '2025-02-20'),
    ('2622', 'my-group', 'MBC Condotel', '2622, Самолет', 69910.02, '2025-09-20'),
    ('2807', 'my-group', 'MBC Condotel', '2807, Двухкомнатная', 139585.98, '2025-09-20');
-- Таблица payment
CREATE TABLE core.payment (
    payment_id   UUID NOT NULL,
    group_id     VARCHAR(255) NOT NULL,
    room_number  VARCHAR(255) NOT NULL,
    amount       NUMERIC(10,2) NOT NULL,
    payment_date DATE NOT NULL,
    category     VARCHAR(255) NOT NULL,
    CONSTRAINT payment_pkey PRIMARY KEY (payment_id),
    CONSTRAINT payment_room_number_fkey 
        FOREIGN KEY (room_number) 
        REFERENCES core.room (room_number)
);