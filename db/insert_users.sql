INSERT INTO "User" (name, email, password_hash, school)
SELECT
    'User ' || i,
    'user' || i || '@example.com',
    'hashed_password_' || i,
    CASE (i % 3)
        WHEN 0 THEN 'NUS'
        WHEN 1 THEN 'NTU'
        ELSE 'SMU'
    END
FROM generate_series(1, 50) AS s(i);

INSERT INTO UserProfile (
    user_id,
    move_in_window,
    lease_preference,
    sleep_schedule,
    cleanliness,
    smoking,
    gender
)
SELECT
    i,

    -- Random move-in date in 2026
    DATE '2026-01-01' + (random() * 60)::INT,

    -- Random lease: 6, 12, 24 months
    (ARRAY[6,12,24])[floor(random()*3 + 1)],

    -- Random sleep schedule
    (ARRAY['early_bird','normal','night_owl'])[floor(random()*3 + 1)]::sleep_schedule_enum,

    -- Random cleanliness
    (ARRAY['low','medium','high'])[floor(random()*3 + 1)]::cleanliness_enum,

    -- Random smoking
    (random() < 0.3),

    -- Random gender
    (ARRAY['male','female'])[floor(random()*2 + 1)]::gender_enum

FROM generate_series(1, 50) AS s(i);