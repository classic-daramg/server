-- Add user_status and deleted_at columns to users table
ALTER TABLE users 
    ADD COLUMN user_status VARCHAR(255) NOT NULL DEFAULT 'ACTIVE',
    ADD COLUMN deleted_at DATETIME(6);
