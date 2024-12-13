-- ============================
-- V1__init_schema.sql
-- Initialize the schema and seed initial data
-- ============================

-- ============================
-- 1. Create Tables
-- ============================

-- Create the "roles" table
CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- Create the "app_users" table
CREATE TABLE app_users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    phone VARCHAR(20),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create the join table for ManyToMany relationship between app_users and roles
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES app_users (id) ON DELETE CASCADE,
    CONSTRAINT fk_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
);

-- Create the "event_type" table
CREATE TABLE event_type (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(500)
);

-- Create the "event" table
CREATE TABLE event (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    recurring BOOLEAN NOT NULL,
    max_participants INT NOT NULL,
    created_by_id BIGINT NOT NULL,
    instructor_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    event_type_id BIGINT NOT NULL,
    CONSTRAINT fk_created_by FOREIGN KEY (created_by_id) REFERENCES app_users (id),
    CONSTRAINT fk_instructor FOREIGN KEY (instructor_id) REFERENCES app_users (id),
    CONSTRAINT fk_event_type FOREIGN KEY (event_type_id) REFERENCES event_type (id)
);

-- Create the "membership_card" table
CREATE TABLE membership_card (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    membership_card_type VARCHAR(50) NOT NULL,
    entrances_left INT NOT NULL CHECK (entrances_left >= 0),
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    paid BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    pending_approval BOOLEAN NOT NULL DEFAULT FALSE,
    price NUMERIC(10, 2) NOT NULL,
    purchase_date TIMESTAMP NOT NULL,
    CONSTRAINT fk_membership_user FOREIGN KEY (user_id) REFERENCES app_users (id)
);

-- ============================
-- 2. Seed Initial Data
-- ============================

-- Insert roles
INSERT INTO roles (name) VALUES
    ('ADMIN'),
    ('INSTRUCTOR'),
    ('CLIENT');

-- Insert admin user (password = "admin")
INSERT INTO app_users (email, password, first_name, last_name, status) VALUES
    ('admin@admin.com', 'admin', 'Admin', 'Admin', 'ACTIVE');

-- Assign roles to admin user
INSERT INTO user_roles (user_id, role_id)
VALUES
    ((SELECT id FROM app_users WHERE email = 'admin@admin.com'), (SELECT id FROM roles WHERE name = 'ADMIN')),
    ((SELECT id FROM app_users WHERE email = 'admin@admin.com'), (SELECT id FROM roles WHERE name = 'INSTRUCTOR'));

-- Insert instructor users (password = "instructor")
INSERT INTO app_users (email, password, first_name, last_name, status) VALUES
    ('instructor1@yoga.com', 'instructor', 'Jane', 'Doe', 'ACTIVE'),
    ('instructor2@yoga.com', 'instructor', 'John', 'Smith', 'ACTIVE'),
    ('instructor3@yoga.com', 'instructor', 'Mary', 'White', 'ACTIVE'),
    ('instructor4@yoga.com', 'instructor', 'Lucas', 'Brown', 'ACTIVE');

-- Assign roles to instructors
INSERT INTO user_roles (user_id, role_id)
SELECT id, (SELECT id FROM roles WHERE name = 'INSTRUCTOR')
FROM app_users
WHERE email LIKE 'instructor%@yoga.com';

-- Insert client users (password = "client")
INSERT INTO app_users (email, password, first_name, last_name, status) VALUES
    ('client1@client.com', 'client', 'Alice', 'Johnson', 'ACTIVE'),
    ('client2@client.com', 'client', 'Bob', 'Brown', 'ACTIVE');

-- Assign roles to clients
INSERT INTO user_roles (user_id, role_id)
SELECT id, (SELECT id FROM roles WHERE name = 'CLIENT')
FROM app_users
WHERE email LIKE 'client%@client.com';

-- Seed Event Types
INSERT INTO event_type (name, description)
VALUES
    ('Yoga', 'Relaxing yoga session.'),
    ('Dance', 'Energetic dance class.'),
    ('Pilates', 'Strengthening Pilates session.'),
    ('CrossFit', 'High-intensity interval training.'),
    ('Meditation', 'Relaxation and mindfulness session.');

-- Seed Events
INSERT INTO event (title, description, start_time, end_time, recurring, max_participants, created_by_id, instructor_id, status, event_type_id)
VALUES
    ('Morning Yoga', 'Morning yoga for all levels.',
        NOW() + INTERVAL '1 DAY' + INTERVAL '7 HOUR', NOW() + INTERVAL '1 DAY' + INTERVAL '8 HOUR',
        FALSE, 20,
        (SELECT id FROM app_users WHERE email = 'admin@admin.com'),
        (SELECT id FROM app_users WHERE email = 'instructor1@yoga.com'),
        'UPCOMING',
        (SELECT id FROM event_type WHERE name = 'Yoga'));