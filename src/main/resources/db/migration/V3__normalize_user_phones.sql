UPDATE user_profiles
SET phone = NULLIF(regexp_replace(phone, '[^0-9+]', '', 'g'), '')
WHERE phone IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS ux_user_profiles_phone
    ON user_profiles (phone)
    WHERE phone IS NOT NULL AND phone <> '';
