CREATE DATABASE IF NOT EXISTS student_management;
USE student_management;

CREATE TABLE IF NOT EXISTS students (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    age TINYINT UNSIGNED NOT NULL CHECK (age BETWEEN 1 AND 120),
    course VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Add a student
INSERT INTO students (name, email, age, course)
VALUES ('Jamie Lee', 'jamie@example.com', 20, 'Computer Science');

-- List all students
SELECT id, name, email, age, course
FROM students
ORDER BY id;

-- Find a student by ID
SELECT id, name, email, age, course
FROM students
WHERE id = 1;

-- Update a student
UPDATE students
SET name = 'Jamie Lee',
    email = 'jamie@example.com',
    age = 21,
    course = 'Information Technology'
WHERE id = 1;

-- Delete a student
DELETE FROM students
WHERE id = 1;

SELECT * from students;
