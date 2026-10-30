-- Honour Your Mentor: Supabase / PostgreSQL Schema Migration V1

CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(64) PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    username VARCHAR(60) NOT NULL UNIQUE,
    email VARCHAR(180) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL DEFAULT 'ROLE_USER',
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_profiles (
    id VARCHAR(64) PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    bio TEXT,
    avatar_uri TEXT,
    cover_color VARCHAR(30) DEFAULT '#54000E',
    phone VARCHAR(30),
    location VARCHAR(120),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id VARCHAR(64) PRIMARY KEY,
    email VARCHAR(180) NOT NULL,
    otp_code VARCHAR(10) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS mentors (
    id VARCHAR(64) PRIMARY KEY,
    created_by VARCHAR(64) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(120) NOT NULL,
    relationship VARCHAR(80) NOT NULL DEFAULT 'Mentor',
    avatar_uri TEXT,
    description TEXT,
    meaningful_quote TEXT,
    item_count INT DEFAULT 0,
    latest_activity VARCHAR(80) DEFAULT 'Recently',
    cover_color VARCHAR(30) DEFAULT '#54000E',
    category VARCHAR(40) DEFAULT 'life',
    duration VARCHAR(60),
    trait VARCHAR(80),
    trait_icon VARCHAR(60),
    lesson_label VARCHAR(120),
    birthday DATE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tributes (
    id VARCHAR(64) PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    mentor_id VARCHAR(64) REFERENCES mentors(id) ON DELETE SET NULL,
    mentor_name VARCHAR(120) NOT NULL,
    kind VARCHAR(30) NOT NULL DEFAULT 'tribute', -- tribute, memory, letter, lesson, milestone
    title VARCHAR(255),
    relationship VARCHAR(80),
    photo_uri TEXT,
    quote TEXT,
    story TEXT,
    excerpt TEXT,
    lesson_carried TEXT,
    moment_to_remember TEXT,
    template_id VARCHAR(60),
    frame_style VARCHAR(60),
    typography_style VARCHAR(60),
    layout_style VARCHAR(60),
    year VARCHAR(20),
    emotion VARCHAR(60),
    emotion_color VARCHAR(30),
    mood VARCHAR(60),
    salutation VARCHAR(120),
    signoff VARCHAR(120),
    signature VARCHAR(120),
    privacy VARCHAR(20) NOT NULL DEFAULT 'PUBLIC', -- PUBLIC, PRIVATE
    is_pinned BOOLEAN NOT NULL DEFAULT FALSE,
    is_shared BOOLEAN NOT NULL DEFAULT FALSE,
    likes_count INT NOT NULL DEFAULT 0,
    comments_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tribute_media (
    id VARCHAR(64) PRIMARY KEY,
    tribute_id VARCHAR(64) NOT NULL REFERENCES tributes(id) ON DELETE CASCADE,
    media_url TEXT NOT NULL,
    media_type VARCHAR(30) NOT NULL DEFAULT 'IMAGE', -- IMAGE, AUDIO, VIDEO
    sort_order INT DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS likes (
    id VARCHAR(64) PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    tribute_id VARCHAR(64) NOT NULL REFERENCES tributes(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_user_tribute_like UNIQUE (user_id, tribute_id)
);

CREATE TABLE IF NOT EXISTS comments (
    id VARCHAR(64) PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    tribute_id VARCHAR(64) NOT NULL REFERENCES tributes(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS reminders (
    id VARCHAR(64) PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    mentor_id VARCHAR(64) REFERENCES mentors(id) ON DELETE CASCADE,
    title VARCHAR(150) NOT NULL,
    reminder_type VARCHAR(40) NOT NULL DEFAULT 'BIRTHDAY', -- BIRTHDAY, ANNIVERSARY, MENTOR_DAY, CUSTOM
    reminder_date DATE NOT NULL,
    notes TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS notifications (
    id VARCHAR(64) PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(150) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(40) DEFAULT 'GENERAL',
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS reports (
    id VARCHAR(64) PRIMARY KEY,
    reporter_id VARCHAR(64) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    tribute_id VARCHAR(64) NOT NULL REFERENCES tributes(id) ON DELETE CASCADE,
    reason TEXT NOT NULL,
    status VARCHAR(30) DEFAULT 'PENDING',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS follows (
    id VARCHAR(64) PRIMARY KEY,
    follower_id VARCHAR(64) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    following_id VARCHAR(64) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_follower_following UNIQUE (follower_id, following_id)
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_mentors_created_by ON mentors(created_by);
CREATE INDEX IF NOT EXISTS idx_tributes_user_id ON tributes(user_id);
CREATE INDEX IF NOT EXISTS idx_tributes_mentor_id ON tributes(mentor_id);
CREATE INDEX IF NOT EXISTS idx_tributes_privacy_created ON tributes(privacy, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_likes_tribute_id ON likes(tribute_id);
CREATE INDEX IF NOT EXISTS idx_comments_tribute_id ON comments(tribute_id);
CREATE INDEX IF NOT EXISTS idx_reminders_user_date ON reminders(user_id, reminder_date);
