-- Создание схемы auth, если она не существует
CREATE SCHEMA IF NOT EXISTS auth;

CREATE TYPE auth.user_role AS ENUM ('admin', 'member');

-- Таблица групп (основная)
CREATE TABLE auth.groups (
    group_id VARCHAR(255) PRIMARY KEY,
    client_id VARCHAR(255) NOT NULL,
    client_secret VARCHAR(255) NOT NULL,
    refresh_token VARCHAR(255) NOT NULL
);

-- Таблица связей пользователей с группами
CREATE TABLE auth.user_groups (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    group_id VARCHAR(255) NOT NULL,
    role auth.user_role NOT NULL DEFAULT 'member',
    joined_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    CONSTRAINT unique_user_group UNIQUE (user_id, group_id),
    
    CONSTRAINT fk_user_groups_group 
        FOREIGN KEY (group_id) 
        REFERENCES auth.groups(group_id) 
        ON DELETE CASCADE
);

CREATE TABLE auth.invite_codes (
    code VARCHAR(64) PRIMARY KEY,
    group_id VARCHAR(255) NOT NULL REFERENCES auth.groups(group_id) ON DELETE CASCADE,
    created_by VARCHAR(255) NOT NULL,  -- user_id админа
    expires_at TIMESTAMP WITH TIME ZONE,
    used_at TIMESTAMP WITH TIME ZONE,  -- NULL = не использован
    used_by VARCHAR(255)
);

CREATE INDEX idx_user_groups_user_id ON auth.user_groups(user_id);
CREATE INDEX idx_user_groups_group_id ON auth.user_groups(group_id);
CREATE INDEX idx_user_groups_role ON auth.user_groups(role);

