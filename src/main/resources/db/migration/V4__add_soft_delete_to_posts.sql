-- Add soft delete columns to posts table
ALTER TABLE posts ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE posts ADD COLUMN deleted_at DATETIME(6);
