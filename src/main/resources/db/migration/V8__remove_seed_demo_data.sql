-- V8: Remove all legacy demo and seed data from Neon PostgreSQL
DELETE FROM tribute_likes WHERE tribute_id IN ('trib_1', 'trib_2') OR user_id = 'u_demo_sarita';
DELETE FROM tribute_comments WHERE tribute_id IN ('trib_1', 'trib_2') OR user_id = 'u_demo_sarita';
DELETE FROM tributes WHERE id IN ('trib_1', 'trib_2') OR user_id = 'u_demo_sarita';
DELETE FROM mentors WHERE id IN ('m1', 'm2', 'm3', 'm4', 'm5') AND created_by = 'u_demo_sarita';
DELETE FROM user_profiles WHERE user_id = 'u_demo_sarita';
DELETE FROM users WHERE id = 'u_demo_sarita';

