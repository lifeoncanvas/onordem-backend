-- V6: Add recipient email, view token, and email delivery audit log
ALTER TABLE tributes ADD COLUMN IF NOT EXISTS recipient_email VARCHAR(160);
ALTER TABLE tributes ADD COLUMN IF NOT EXISTS view_token VARCHAR(64);
CREATE INDEX IF NOT EXISTS idx_tributes_view_token ON tributes(view_token);

CREATE TABLE IF NOT EXISTS email_delivery_logs (
    id VARCHAR(64) PRIMARY KEY,
    recipient VARCHAR(160) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    email_type VARCHAR(60) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'SENT', -- SENT, FAILED, LOGGED
    error_message TEXT,
    sent_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_email_logs_recipient ON email_delivery_logs(recipient);
CREATE INDEX IF NOT EXISTS idx_email_logs_sent_at ON email_delivery_logs(sent_at);
