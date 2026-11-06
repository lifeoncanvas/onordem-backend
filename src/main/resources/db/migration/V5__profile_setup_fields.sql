-- Profile setup answers captured right after sign-up.
ALTER TABLE user_profiles ADD COLUMN IF NOT EXISTS birthday DATE;
ALTER TABLE user_profiles ADD COLUMN IF NOT EXISTS gender VARCHAR(40);
ALTER TABLE user_profiles ADD COLUMN IF NOT EXISTS honouring TEXT;
ALTER TABLE user_profiles ADD COLUMN IF NOT EXISTS setup_complete BOOLEAN NOT NULL DEFAULT FALSE;
