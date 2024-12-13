-- ============================
-- V2__create_additional_tables.sql
-- Create additional tables
-- ============================

-- Create the "attendance" table
CREATE TABLE attendance (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    event_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_attendance_user FOREIGN KEY (user_id) REFERENCES app_users (id) ON DELETE CASCADE,
    CONSTRAINT fk_attendance_event FOREIGN KEY (event_id) REFERENCES event (id) ON DELETE CASCADE
);

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
    CONSTRAINT fk_event_history_user FOREIGN KEY (user_id) REFERENCES app_users (id) ON DELETE CASCADE,
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
    CONSTRAINT fk_card_history_user FOREIGN KEY (user_id) REFERENCES app_users (id) ON DELETE CASCADE
);