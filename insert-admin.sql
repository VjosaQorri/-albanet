s -- ========================================
-- SQL Query to Insert Admin Staff Member
-- ========================================
-- Database: albanet_db
-- Table: staff
-- Email: admin@admin.com
-- Password: secretadmin
-- Role: ADMIN
-- ========================================

INSERT INTO staff (
    first_name,
    last_name,
    email,
    password,
    phone_number,
    employee_number,
    role,
    hired_at,
    active,
    last_login_at
) VALUES (
    'Admin',
    'Administrator',
    'admin@admin.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMye1p0iy1RVaXiWI1HfFOlOjzReV91QFEK',  -- BCrypt hash for 'secretadmin'
    '+355 69 999 9999',
    'EMP-ADMIN-001',
    'ADMIN',
    NOW(),
    true,
    NULL
);

-- ========================================
-- HOW TO USE:
-- ========================================
-- 1. Open PostgreSQL terminal:
--    psql -U albanet -d albanet_db
--
-- 2. Copy and paste the INSERT query above
--
-- 3. Press Enter to execute
--
-- 4. Login to your application with:
--    Email: admin@admin.com
--    Password: secretadmin
-- ========================================

-- To verify the insertion:
-- SELECT * FROM staff WHERE email = 'admin@admin.com';

