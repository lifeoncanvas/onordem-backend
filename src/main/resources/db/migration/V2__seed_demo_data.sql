-- Honour Your Mentor: Seed Demo Data V2

-- 1. Demo User: Sarita Baiga (matches frontend UserProfile)
-- Password: password123 ($2a$10$7EqJtq98hPqEX7fNZaFWoO.8sO6p1k1.nU33t9yO19b3o9d7G1HWW is BCrypt for 'password123')
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

-- 2. Seed Mentors from frontend home.tsx
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

-- 3. Seed Initial Tributes / Memories
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
