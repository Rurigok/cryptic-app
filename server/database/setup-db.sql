-- Rebuild database
DROP DATABASE IF EXISTS cryptic;
CREATE DATABASE cryptic;

-- Create standard user
CREATE USER cryptic_user IDENTIFIED BY 'deployment_password';

-- Setup tables

-- Add testing/development data

-- Grant user privileges
GRANT SELECT, INSERT, UPDATE, DELETE ON cryptic.users TO 'cryptic_user'@'localhost';
