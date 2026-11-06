-- Track wrong OTP guesses per reset token so 5-digit codes can't be brute-forced.
ALTER TABLE password_reset_tokens ADD COLUMN IF NOT EXISTS failed_attempts INTEGER NOT NULL DEFAULT 0;

-- Burn any reset codes issued before this change.
UPDATE password_reset_tokens SET used = TRUE WHERE used = FALSE;
