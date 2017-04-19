-- Rebuild database
DROP DATABASE IF EXISTS cryptic;
CREATE DATABASE cryptic;

-- Create standard user
DROP USER 'cryptic_user'@'%';
DROP USER 'cryptic_user'@'localhost';
CREATE USER 'cryptic_user'@'%' IDENTIFIED BY 'deployment_password';
CREATE USER 'cryptic_user'@'localhost' IDENTIFIED BY 'deployment_password';

-- Setup tables

-- Add testing/development data

-- Grant user privileges
GRANT SELECT, INSERT, UPDATE, DELETE ON cryptic.users TO 'cryptic_user'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON cryptic.users TO 'cryptic_user'@'localhost';
