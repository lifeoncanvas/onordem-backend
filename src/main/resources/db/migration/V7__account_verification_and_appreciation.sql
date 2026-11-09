-- V7: Account verification OTP tokens and appreciation notification tracking
CREATE TABLE IF NOT EXISTS account_verification_tokens (
    id VARCHAR(64) PRIMARY KEY,
    email VARCHAR(180) NOT NULL,
    otp_code VARCHAR(10) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    failed_attempts INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_acc_ver_tokens_email ON account_verification_tokens(email);

CREATE TABLE IF NOT EXISTS appreciation_notifications (
    id VARCHAR(64) PRIMARY KEY,
    sender_id VARCHAR(64) NOT NULL,
    recipient_id VARCHAR(64) NOT NULL,
    tribute_id VARCHAR(64),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_apprec_notif_sender_recip ON appreciation_notifications(sender_id, recipient_id);

