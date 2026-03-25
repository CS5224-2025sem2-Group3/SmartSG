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