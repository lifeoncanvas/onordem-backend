-- ==============================================================================
-- HONOUR YOUR MENTOR: SUPABASE POSTGRESQL SCHEMA & INITIAL DATA
-- Copy and paste this directly into Supabase Dashboard -> SQL Editor and click "RUN"
-- ==============================================================================

-- 1. USERS & PROFILES
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

-- 2. MENTORS
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

-- 3. TRIBUTES & MEMORIES (Supports tribute, memory, letter, lesson, milestone)
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

-- 4. ENGAGEMENT (Likes & Comments)
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

-- 5. REMINDERS & NOTIFICATIONS
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

-- INDEXES
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_mentors_created_by ON mentors(created_by);
CREATE INDEX IF NOT EXISTS idx_tributes_user_id ON tributes(user_id);
CREATE INDEX IF NOT EXISTS idx_tributes_mentor_id ON tributes(mentor_id);
CREATE INDEX IF NOT EXISTS idx_tributes_privacy_created ON tributes(privacy, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_likes_tribute_id ON likes(tribute_id);
CREATE INDEX IF NOT EXISTS idx_comments_tribute_id ON comments(tribute_id);
CREATE INDEX IF NOT EXISTS idx_reminders_user_date ON reminders(user_id, reminder_date);

-- DEMO SEED DATA
INSERT INTO users (id, name, username, email, password_hash, role, enabled, created_at, updated_at)
VALUES (
    'u_demo_sarita',
    'Sarita Baiga',
    'saritabaiga',
    'sarita@example.com',
    '$2a$10$wN1rWvXlPZ/aV861L5e5ueU7k8U5c25v8C7bVsm8J6Jg4lU4m8aKG',
    'ROLE_USER',
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (id) DO NOTHING;

INSERT INTO user_profiles (id, user_id, bio, avatar_uri, cover_color, phone, location, created_at, updated_at)
VALUES (
    'up_demo_sarita',
    'u_demo_sarita',
    'Remembering the people who helped me grow.',
    'https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=400&auto=format&fit=crop&q=80',
    '#54000E',
    '+1 555-0199',
    'San Francisco, CA',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (id) DO NOTHING;

INSERT INTO mentors (id, created_by, name, relationship, avatar_uri, description, meaningful_quote, item_count, latest_activity, cover_color, category, duration, trait, trait_icon, lesson_label, created_at)
VALUES
(
    'm1',
    'u_demo_sarita',
    'Mrs. Anita Sharma',
    'Literature Teacher',
    'https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=600&auto=format&fit=crop&q=80',
    'High school teacher who opened the door to storytelling and critical reflection.',
    'Believe in your voice before asking the world to listen.',
    14,
    '3 days ago',
    '#1B4D3E',
    'school',
    '3 years together',
    'Patient guide',
    'flask-outline',
    'She stayed after class until fractions finally clicked.',
    CURRENT_TIMESTAMP
),
(
    'm2',
    'u_demo_sarita',
    'Dr. David Mitchell',
    'Research Advisor',
    'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=600&auto=format&fit=crop&q=80',
    'Guided my master thesis and taught how to ask rigorous scientific questions.',
    'True mastery is quiet patience with difficult problems.',
    9,
    '1 week ago',
    '#3B2F63',
    'study',
    '2 years together',
    'Honest compass',
    'compass-outline',
    'Critique is an invitation to who you can become tomorrow.',
    CURRENT_TIMESTAMP
),
(
    'm3',
    'u_demo_sarita',
    'Vikram Rao',
    'Engineering Director',
    'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=600&auto=format&fit=crop&q=80',
    'Engineering leader who demonstrated empathy and technical excellence in high-stakes projects.',
    'Build things that outlive your tenure and elevate people.',
    18,
    'Yesterday',
    '#2E5D4B',
    'work',
    '6 years together',
    'Master craftsman',
    'construct-outline',
    'Build things that outlive your tenure and elevate people around you.',
    CURRENT_TIMESTAMP
),
(
    'm4',
    'u_demo_sarita',
    'Elena Rostova',
    'Art & Design Coach',
    'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=600&auto=format&fit=crop&q=80',
    'Mentor who helped unlock raw design expression and fearless prototyping.',
    'Do not fear the blank page; it is waiting for your courage.',
    11,
    '2 weeks ago',
    '#682D42',
    'life',
    '5 years together',
    'Creative clarity',
    'sparkles-outline',
    'Forgive yourself for the first five ugly drafts to find the single true stroke.',
    CURRENT_TIMESTAMP
),
(
    'm5',
    'u_demo_sarita',
    'Priya Nair',
    'Design Lead',
    'https://images.unsplash.com/photo-1531746020798-e6953c6e8e04?w=600&auto=format&fit=crop&q=80',
    'Product design leader who advocated for user-first storytelling.',
    'Trust your taste and ship the idea anyway.',
    7,
    '5 days ago',
    '#7D3C1B',
    'work',
    '4 years together',
    'Creative visionary',
    'color-palette-outline',
    'She told me to trust my taste and ship the idea anyway.',
    CURRENT_TIMESTAMP
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO tributes (
    id, user_id, mentor_id, mentor_name, kind, title, relationship, photo_uri, quote,
    story, lesson_carried, moment_to_remember, template_id, frame_style, typography_style,
    privacy, is_pinned, is_shared, likes_count, comments_count, created_at
) VALUES
(
    'trib_1',
    'u_demo_sarita',
    'm1',
    'Mrs. Anita Sharma',
    'tribute',
    'The Teacher Who Taught Me How to Write and Believe',
    'Literature Teacher',
    'https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=600&auto=format&fit=crop&q=80',
    'Believe in your voice before asking the world to listen.',
    'When everyone gave up on my essays, she spent hours in the library teaching me cadence, clarity, and kindness.',
    'Care enough about your craft to revise without defense.',
    'The rainy Tuesday afternoon she handed me my first published poem.',
    'classic-elegance',
    'gold-leaf',
    'serif-editorial',
    'PUBLIC',
    TRUE,
    TRUE,
    24,
    6,
    CURRENT_TIMESTAMP
),
(
    'trib_2',
    'u_demo_sarita',
    'm3',
    'Vikram Rao',
    'memory',
    'The Late Night Production Outage We Solved Together',
    'Engineering Director',
    'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=600&auto=format&fit=crop&q=80',
    'Panicking fixes zero bugs. Breathe, read the logs, and protect the team.',
    'During our biggest Black Friday outage, Vikram bought tea for the whole war room and showed us how to trace issues calmly.',
    'Calm leadership in crises is infectious.',
    'The moment the servers turned green at 3:14 AM.',
    'modern-bold',
    'clean-minimal',
    'sans-modern',
    'PUBLIC',
    TRUE,
    TRUE,
    42,
    11,
    CURRENT_TIMESTAMP
)
ON CONFLICT (id) DO NOTHING;
