-- ============================
-- V3__seed_events.sql
-- Seed events into the database
-- ============================

INSERT INTO event (title, description, start_time, end_time, recurring, max_participants, created_by_id, instructor_id, status, event_type_id)
VALUES
    ('Morning Yoga', 'Morning yoga for all levels.',
        NOW() + INTERVAL '1 DAY' + INTERVAL '7 HOUR', NOW() + INTERVAL '1 DAY' + INTERVAL '8 HOUR',
        FALSE, 20,
        (SELECT id FROM app_users WHERE email = 'admin@admin.com'),
        (SELECT id FROM app_users WHERE email = 'instructor1@yoga.com'),
        'UPCOMING',
        (SELECT id FROM event_type WHERE name = 'Yoga')),

    ('Evening Dance', 'Fun evening dance class.',
        NOW() + INTERVAL '1 DAY' + INTERVAL '18 HOUR', NOW() + INTERVAL '1 DAY' + INTERVAL '19 HOUR',
        FALSE, 20,
        (SELECT id FROM app_users WHERE email = 'admin@admin.com'),
        (SELECT id FROM app_users WHERE email = 'instructor2@yoga.com'),
        'UPCOMING',
        (SELECT id FROM event_type WHERE name = 'Dance')),

    ('Pilates for Beginners', 'Introduction to Pilates.',
        NOW() + INTERVAL '2 DAY' + INTERVAL '10 HOUR', NOW() + INTERVAL '2 DAY' + INTERVAL '11 HOUR',
        FALSE, 20,
        (SELECT id FROM app_users WHERE email = 'admin@admin.com'),
        (SELECT id FROM app_users WHERE email = 'instructor3@yoga.com'),
        'UPCOMING',
        (SELECT id FROM event_type WHERE name = 'Pilates')),

    ('CrossFit Extreme', 'Push your limits with this CrossFit session.',
        NOW() + INTERVAL '3 DAY' + INTERVAL '6 HOUR', NOW() + INTERVAL '3 DAY' + INTERVAL '7 HOUR',
        FALSE, 20,
        (SELECT id FROM app_users WHERE email = 'admin@admin.com'),
        (SELECT id FROM app_users WHERE email = 'instructor4@yoga.com'),
        'UPCOMING',
        (SELECT id FROM event_type WHERE name = 'CrossFit'));