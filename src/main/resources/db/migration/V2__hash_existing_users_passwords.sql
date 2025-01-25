-- Update admin user's password to a hashed version
UPDATE app_user
SET password = '$2a$12$JAkc4iE85VbPq/UgzmXJd.3D9a1zt4kE78AsaohQqnHzmDEm/guo6' -- bcrypt hash for "admin"
WHERE email = 'admin@admin.com';

-- Update instructor users' passwords to a hashed version
UPDATE app_user
SET password = '$2a$12$EjFWDzJIW7JlkAJYsA6yIuy/MwjqrtxFE.RJee.N.OJ/ScJ.tGbLa' -- bcrypt hash for "instructor"
WHERE email LIKE 'instructor%@yoga.com';

-- Update client users' passwords to a hashed version
UPDATE app_user
SET password = '$2a$12$JAkc4iE85VbPq/UgzmXJd.3D9a1zt4kE78AsaohQqnHzmDEm/guo6' -- bcrypt hash for "admin"
WHERE email LIKE 'client%@client.com';
