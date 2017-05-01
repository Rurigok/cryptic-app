-- Rebuild database
DROP DATABASE IF EXISTS cryptic;
CREATE DATABASE cryptic;

-- Create standard user
DROP USER 'cryptic_user'@'%';
DROP USER 'cryptic_user'@'localhost';
CREATE USER 'cryptic_user'@'%' IDENTIFIED BY 'deployment_password';
CREATE USER 'cryptic_user'@'localhost' IDENTIFIED BY 'deployment_password';

-- Setup tables
CREATE TABLE IF NOT EXISTS cryptic.users (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(255) NOT NULL,
  password VARCHAR(255) NOT NULL,
  personal_key VARCHAR(2048),
  public_key VARCHAR(2048),
  is_admin TINYINT DEFAULT 0
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS cryptic.directory (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT UNSIGNED NOT NULL,
  CONSTRAINT fk_user_id
    FOREIGN KEY (user_id) REFERENCES users (id)
    ON DELETE CASCADE
    ON UPDATE RESTRICT,
  device_ip VARCHAR(255)
) ENGINE = InnoDB;

-- Grant database user privileges
GRANT SELECT, INSERT, UPDATE, DELETE ON cryptic.users TO 'cryptic_user'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON cryptic.users TO 'cryptic_user'@'localhost';

-- Add testing/development data
-- TestUser : testpassword
INSERT INTO cryptic.users (username, password)
  VALUES ('TestUser', '$2b$12$7nwQBIK.HVaGTnbV5ahdouqp34Tt2660W4/7sqg5qbEgwxdvHncFi');
-- TestAdmin : testpassword
INSERT INTO cryptic.users (username, password, is_admin)
  VALUES ('TestAdmin', '$2b$12$7nwQBIK.HVaGTnbV5ahdouqp34Tt2660W4/7sqg5qbEgwxdvHncFi', 10);
