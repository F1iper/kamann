-- ============================
-- V1__init_schema.sql
-- Initialize the schema and seed initial data
-- ============================

-- ============================
-- 1. Create Tables
-- ============================

-- Create the "role" table
CREATE TABLE role (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- Create the "app_user" table
CREATE TABLE app_user (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    phone VARCHAR(20),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Create the join table for ManyToMany relationship between app_user and role
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES app_user (id) ON DELETE CASCADE,
    CONSTRAINT fk_role FOREIGN KEY (role_id) REFERENCES role (id) ON DELETE CASCADE
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

    -- Date and time fields
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    time TIME NOT NULL,

    -- Event configuration
    recurring BOOLEAN NOT NULL,
    max_participants INT NOT NULL,
    current_participants INT NOT NULL DEFAULT 0,
    status VARCHAR(50) NOT NULL,

    -- Foreign keys
    created_by_id BIGINT NOT NULL,
    instructor_id BIGINT NOT NULL,
    event_type_id BIGINT NOT NULL,

    CONSTRAINT fk_created_by FOREIGN KEY (created_by_id) REFERENCES app_user (id),
    CONSTRAINT fk_instructor FOREIGN KEY (instructor_id) REFERENCES app_user (id),
    CONSTRAINT fk_event_type FOREIGN KEY (event_type_id) REFERENCES event_type (id)
);

-- Recurrence configuration as embedded
ALTER TABLE event ADD COLUMN recurrence_frequency VARCHAR(50);
ALTER TABLE event ADD COLUMN recurrence_days_of_week VARCHAR(255);
ALTER TABLE event ADD COLUMN recurrence_end_date DATE;

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
    CONSTRAINT fk_membership_user FOREIGN KEY (user_id) REFERENCES app_user (id)
);

-- Create the "attendance" table
CREATE TABLE attendance (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    event_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign key constraints with cascade delete
    CONSTRAINT fk_attendance_user FOREIGN KEY (user_id) REFERENCES app_user (id) ON DELETE CASCADE,
    CONSTRAINT fk_attendance_event FOREIGN KEY (event_id) REFERENCES event (id) ON DELETE CASCADE,

    -- Prevent duplicate attendance records for the same user and event
    CONSTRAINT unique_user_event UNIQUE (user_id, event_id)
);

-- Index for faster lookups
CREATE INDEX idx_attendance_user_event ON attendance(user_id, event_id);

-- Create the "client_event_history" table
CREATE TABLE client_event_history (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    event_id BIGINT NOT NULL,
    status VARCHAR(50),
    attended_date TIMESTAMP,
    entrances_used INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_event_history_user FOREIGN KEY (user_id) REFERENCES app_user (id) ON DELETE CASCADE,
    CONSTRAINT fk_event_history_event FOREIGN KEY (event_id) REFERENCES event (id) ON DELETE CASCADE
);

-- Create the "client_membership_card_history" table
CREATE TABLE client_membership_card_history (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    membership_card_type VARCHAR(50) NOT NULL,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    entrances INT NOT NULL,
    remaining_entrances INT NOT NULL,
    paid BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_card_history_user FOREIGN KEY (user_id) REFERENCES app_user (id) ON DELETE CASCADE
);

-- Create the "event_stat" table
CREATE TABLE event_stat (
    id BIGSERIAL PRIMARY KEY,
    event_type VARCHAR(50) NOT NULL,
    total_events INT NOT NULL DEFAULT 0,
    completed_events INT NOT NULL DEFAULT 0,
    cancelled_events INT NOT NULL DEFAULT 0
);

-- Create the "attendance_stat" table
CREATE TABLE attendance_stat (
    id BIGSERIAL PRIMARY KEY,
    event_name VARCHAR(255) NOT NULL,
    total_participants INT NOT NULL DEFAULT 0,
    attended INT NOT NULL DEFAULT 0,
    absent INT NOT NULL DEFAULT 0,
    late_cancellations INT NOT NULL DEFAULT 0
);

-- Create the "revenue_stat" table
CREATE TABLE revenue_stat (
    id BIGSERIAL PRIMARY KEY,
    membership_type VARCHAR(50) NOT NULL,
    total_revenue NUMERIC(10, 2) NOT NULL DEFAULT 0,
    total_transactions INT NOT NULL DEFAULT 0
);

-- Create the "user_event_registration" table
CREATE TABLE user_event_registration (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    event_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    waitlist_position INT DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_registration_user FOREIGN KEY (user_id) REFERENCES app_user (id) ON DELETE CASCADE,
    CONSTRAINT fk_registration_event FOREIGN KEY (event_id) REFERENCES event (id) ON DELETE CASCADE
);

-- ============================
-- 2. Seed Initial Data
-- ============================

-- Insert roles
INSERT INTO role (name) VALUES
    ('ADMIN'),
    ('INSTRUCTOR'),
    ('CLIENT');

-- Insert admin user with plain text password
INSERT INTO app_user (email, password, first_name, last_name, status) VALUES
    ('admin@admin.com', 'admin', 'Neo', 'Matrix', 'ACTIVE');

-- Assign roles to admin user
INSERT INTO user_roles (user_id, role_id)
VALUES
    ((SELECT id FROM app_user WHERE email = 'admin@admin.com'), (SELECT id FROM role WHERE name = 'ADMIN')),
    ((SELECT id FROM app_user WHERE email = 'admin@admin.com'), (SELECT id FROM role WHERE name = 'INSTRUCTOR'));

-- Insert instructor users with plain text password
INSERT INTO app_user (email, password, first_name, last_name, status) VALUES
    ('instructor1@yoga.com', 'instructor', 'Jane', 'Doe', 'ACTIVE'),
    ('instructor2@yoga.com', 'instructor', 'John', 'Smith', 'ACTIVE'),
    ('instructor3@yoga.com', 'instructor', 'Mary', 'White', 'ACTIVE'),
    ('instructor4@yoga.com', 'instructor', 'Lucas', 'Brown', 'ACTIVE');

-- Assign roles to instructors
INSERT INTO user_roles (user_id, role_id)
SELECT id, (SELECT id FROM role WHERE name = 'INSTRUCTOR')
FROM app_user
WHERE email LIKE 'instructor%@yoga.com';

-- Insert client users with plain text password
INSERT INTO app_user (email, password, first_name, last_name, status)
VALUES
    ('client1@client.com', 'password123', 'Client1', 'Test', 'ACTIVE'),
    ('client2@client.com', 'password123', 'Client2', 'Test', 'ACTIVE'),
    ('client3@client.com', 'password123', 'Client3', 'Test', 'ACTIVE'),
    ('client4@client.com', 'password123', 'Client4', 'Test', 'ACTIVE');

-- Assign roles to clients
INSERT INTO user_roles (user_id, role_id)
SELECT id, (SELECT id FROM role WHERE name = 'CLIENT')
FROM app_user
WHERE email LIKE 'client%@client.com';

-- Seed Event Types
INSERT INTO event_type (name, description)
VALUES
    ('Yoga', 'Yoga classes for all levels.'),
    ('Dance', 'Fun and energetic dance classes.'),
    ('Pilates', 'Introduction to Pilates.'),
    ('CrossFit', 'High-intensity CrossFit sessions.'),
    ('Strength Training', 'Build strength with guided exercises.'),
    ('Stretching', 'Relaxing stretching sessions.');

INSERT INTO event (
     title, description, start_date, end_date, time, recurring, recurrence_frequency, recurrence_days_of_week,
     max_participants, created_by_id, instructor_id, status, event_type_id
 )
 VALUES
     -- Past Events (One-Time)
     ('Morning Yoga', 'Morning yoga for all levels.',
         NOW()::date - INTERVAL '5 DAYS', NOW()::date - INTERVAL '5 DAYS', '07:00:00',
         FALSE, NULL, NULL, 20,
         (SELECT id FROM app_user WHERE email = 'admin@admin.com'),
         (SELECT id FROM app_user WHERE email = 'instructor1@yoga.com'),
         'COMPLETED',
         (SELECT id FROM event_type WHERE name = 'Yoga')),

     ('Evening Dance', 'Fun evening dance class.',
         NOW()::date - INTERVAL '3 DAYS', NOW()::date - INTERVAL '3 DAYS', '18:00:00',
         FALSE, NULL, NULL, 20,
         (SELECT id FROM app_user WHERE email = 'admin@admin.com'),
         (SELECT id FROM app_user WHERE email = 'instructor2@yoga.com'),
         'COMPLETED',
         (SELECT id FROM event_type WHERE name = 'Dance')),

     -- Upcoming Events (One-Time)
     ('Pilates for Beginners', 'Introduction to Pilates.',
         NOW()::date + INTERVAL '2 DAYS', NOW()::date + INTERVAL '2 DAYS', '10:00:00',
         FALSE, NULL, NULL, 20,
         (SELECT id FROM app_user WHERE email = 'admin@admin.com'),
         (SELECT id FROM app_user WHERE email = 'instructor3@yoga.com'),
         'UPCOMING',
         (SELECT id FROM event_type WHERE name = 'Pilates')),

     ('CrossFit Extreme', 'Push your limits with this CrossFit session.',
         NOW()::date + INTERVAL '3 DAYS', NOW()::date + INTERVAL '3 DAYS', '06:00:00',
         FALSE, NULL, NULL,20,
         (SELECT id FROM app_user WHERE email = 'admin@admin.com'),
         (SELECT id FROM app_user WHERE email = 'instructor4@yoga.com'),
         'UPCOMING',
         (SELECT id FROM event_type WHERE name = 'CrossFit')),

     -- Recurring Events (Weekly, 3 Months into the Future)
     ('Weekly Yoga', 'Relaxing yoga session every week.',
         NOW()::date, NOW()::date + INTERVAL '3 MONTHS', '07:00:00',
         TRUE, 'WEEKLY', 'MONDAY,WEDNESDAY',20,
         (SELECT id FROM app_user WHERE email = 'admin@admin.com'),
         (SELECT id FROM app_user WHERE email = 'instructor1@yoga.com'),
         'UPCOMING',
         (SELECT id FROM event_type WHERE name = 'Yoga')),

     ('Monthly Strength Training', 'Build strength with monthly sessions.',
         NOW()::date, NOW()::date + INTERVAL '3 MONTHS', '18:00:00',
         TRUE, 'MONTHLY', NULL, 20,
         (SELECT id FROM app_user WHERE email = 'admin@admin.com'),
         (SELECT id FROM app_user WHERE email = 'instructor2@yoga.com'),
         'UPCOMING',
         (SELECT id FROM event_type WHERE name = 'Strength Training')),

     ('Daily Morning Stretch', 'Start your day with a quick stretch.',
         NOW()::date, NOW()::date + INTERVAL '3 MONTHS', '06:30:00',
         TRUE, 'DAILY', NULL, 20,
         (SELECT id FROM app_user WHERE email = 'admin@admin.com'),
         (SELECT id FROM app_user WHERE email = 'instructor3@yoga.com'),
         'UPCOMING',
         (SELECT id FROM event_type WHERE name = 'Stretching'));


-- Seed membership cards into the database
INSERT INTO membership_card (
user_id,
membership_card_type,
entrances_left,
start_date,
end_date,
paid,
active,
pending_approval,
price,
purchase_date
)
VALUES
(
    (SELECT id FROM app_user WHERE email = 'admin@admin.com'),
    'SINGLE_ENTRY',
    1,
    NOW(),
    NOW() + INTERVAL '30 days',
    FALSE,
    FALSE,
    FALSE,
    10.00,
    NOW()
),
(
    (SELECT id FROM app_user WHERE email = 'admin@admin.com'),
    'MONTHLY_4',
    4,
    NOW(),
    NOW() + INTERVAL '30 days',
    FALSE,
    FALSE,
    FALSE,
    20.00,
    NOW()
),
(
    (SELECT id FROM app_user WHERE email = 'admin@admin.com'),
    'MONTHLY_8',
    8,
    NOW(),
    NOW() + INTERVAL '30 days',
    FALSE,
    FALSE,
    FALSE,
    40.00,
    NOW()
),
(
    (SELECT id FROM app_user WHERE email = 'admin@admin.com'),
    'MONTHLY_12',
    12,
    NOW(),
    NOW() + INTERVAL '30 days',
    FALSE,
    FALSE,
    FALSE,
    60.00,
    NOW()
);

-- Seed test membership cards with different types for different users
INSERT INTO membership_card (
    user_id,
    membership_card_type,
    entrances_left,
    start_date,
    end_date,
    purchase_date,
    price,
    paid,
    active
)
VALUES
    (
        (SELECT id FROM app_user WHERE email = 'client1@client.com'),
        'SINGLE_ENTRY',
        1,
        NOW(),
        NOW() + INTERVAL '1 MONTH',
        NOW(),
        10.00,
        TRUE,
        TRUE
    ),
    (
        (SELECT id FROM app_user WHERE email = 'client2@client.com'),
        'MONTHLY_4',
        4,
        NOW(),
        NOW() + INTERVAL '1 MONTH',
        NOW(),
        20.00,
        TRUE,
        TRUE
    ),
    (
        (SELECT id FROM app_user WHERE email = 'client3@client.com'),
        'MONTHLY_8',
        8,
        NOW(),
        NOW() + INTERVAL '1 MONTH',
        NOW(),
        40.00,
        TRUE,
        TRUE
    ),
    (
        (SELECT id FROM app_user WHERE email = 'client4@client.com'),
        'MONTHLY_12',
        12,
        NOW(),
        NOW() + INTERVAL '1 MONTH',
        NOW(),
        60.00,
        TRUE,
        TRUE
    );

-- Seed event statistics
INSERT INTO event_stat (event_type, total_events, completed_events, cancelled_events)
VALUES
    ('Yoga', 50, 45, 5),
    ('Dance', 30, 28, 2),
    ('Pilates', 20, 18, 2),
    ('CrossFit', 25, 20, 5),
    ('Meditation', 15, 15, 0);

-- Seed attendance statistics
INSERT INTO attendance_stat (event_name, total_participants, attended, absent, late_cancellations)
VALUES
    ('Morning Yoga', 40, 35, 3, 2),
    ('Evening Dance', 25, 22, 2, 1),
    ('Pilates for Beginners', 18, 15, 2, 1),
    ('CrossFit Extreme', 20, 18, 1, 1),
    ('Mindful Meditation', 15, 14, 1, 0);

-- Seed revenue statistics
INSERT INTO revenue_stat (membership_type, total_revenue, total_transactions)
VALUES
    ('MONTHLY_4', 5000, 100),
    ('MONTHLY_8', 2000, 50),
    ('SINGLE_ENTRY', 1000, 20);

-- Seed user event registrations
INSERT INTO user_event_registration (user_id, event_id, status, waitlist_position)
VALUES
    ((SELECT id FROM app_user WHERE email = 'client1@client.com'),
     (SELECT id FROM event WHERE title = 'Morning Yoga'),
     'REGISTERED', NULL),
    ((SELECT id FROM app_user WHERE email = 'client2@client.com'),
     (SELECT id FROM event WHERE title = 'Evening Dance'),
     'REGISTERED', NULL),
    ((SELECT id FROM app_user WHERE email = 'client3@client.com'),
     (SELECT id FROM event WHERE title = 'Pilates for Beginners'),
     'REGISTERED', 1),
    ((SELECT id FROM app_user WHERE email = 'client4@client.com'),
     (SELECT id FROM event WHERE title = 'CrossFit Extreme'),
     'WAITLISTED', 2);


 -- Seed client event histories
 INSERT INTO client_event_history (user_id, event_id, status, attended_date, entrances_used, created_at, updated_at)
 VALUES
     ((SELECT id FROM app_user WHERE email = 'client1@client.com'),
      (SELECT id FROM event WHERE title = 'Morning Yoga'),
      'PRESENT', NOW() - INTERVAL '1 DAY', 1, NOW(), NOW()),
     ((SELECT id FROM app_user WHERE email = 'client2@client.com'),
      (SELECT id FROM event WHERE title = 'Evening Dance'),
      'LATE_CANCEL', NOW() - INTERVAL '2 DAY', 0, NOW(), NOW()),
     ((SELECT id FROM app_user WHERE email = 'client3@client.com'),
      (SELECT id FROM event WHERE title = 'Pilates for Beginners'),
      'ABSENT', NOW() - INTERVAL '3 DAY', 0, NOW(), NOW()),
     ((SELECT id FROM app_user WHERE email = 'client4@client.com'),
      (SELECT id FROM event WHERE title = 'CrossFit Extreme'),
      'PRESENT', NOW() - INTERVAL '4 DAY', 1, NOW(), NOW());

-- Seed client membership card histories
INSERT INTO client_membership_card_history (user_id, membership_card_type, start_date, end_date, entrances, remaining_entrances, paid)
VALUES
    ((SELECT id FROM app_user WHERE email = 'client1@client.com'),
     'MONTHLY_4', NOW() - INTERVAL '1 MONTH', NOW(), 4, 2, TRUE),
    ((SELECT id FROM app_user WHERE email = 'client2@client.com'),
     'MONTHLY_8', NOW() - INTERVAL '2 MONTHS', NOW(), 8, 1, TRUE),
    ((SELECT id FROM app_user WHERE email = 'client3@client.com'),
     'SINGLE_ENTRY', NOW() - INTERVAL '10 DAYS', NOW(), 1, 0, TRUE);

