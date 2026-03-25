INSERT INTO UserProfile (
    user_id,
    move_in_window,
    lease_preference,
    sleep_schedule,
    cleanliness,
    smoking,
    gender,
    max_budget
)
SELECT
    u.user_id,  -- real user_id from User table
    DATE '2026-01-01' + (random() * 60)::INT,
    (ARRAY[6,12,24])[floor(random()*3 + 1)],
    (ARRAY['early_bird','normal','night_owl'])[floor(random()*3 + 1)]::sleep_schedule_enum,
    (ARRAY['low','medium','high'])[floor(random()*3 + 1)]::cleanliness_enum,
    (random() < 0.3),
    (ARRAY['male','female'])[floor(random()*2 + 1)]::gender_enum,
    1000 + (FLOOR(RANDOM() * 11) * 100)::int
FROM "User" u
ORDER BY u.user_id
LIMIT 50;