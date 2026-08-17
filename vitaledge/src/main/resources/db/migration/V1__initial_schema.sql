-- VitalEdge initial schema (port of the source project's 0001_initial_schema.py)
-- JSON values use jsonb; enums are stored as varchar.

CREATE TABLE users (
    id uuid PRIMARY KEY,
    email varchar(255) NOT NULL UNIQUE,
    password_hash varchar(255) NOT NULL,
    full_name varchar(120),
    status varchar(30) NOT NULL DEFAULT 'pending',
    is_verified boolean NOT NULL DEFAULT FALSE,
    is_active boolean NOT NULL DEFAULT TRUE,
    is_locked boolean NOT NULL DEFAULT FALSE,
    last_login_at timestamptz,
    last_login_ip varchar(45),
    preferred_language varchar(10) DEFAULT 'en',
    notification_preferences text,
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL
);

CREATE TABLE roles (
    id uuid PRIMARY KEY,
    name varchar(50) NOT NULL UNIQUE,
    role_type varchar(30) NOT NULL,
    description varchar(255),
    is_active boolean NOT NULL DEFAULT TRUE,
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL
);

CREATE TABLE permissions (
    id uuid PRIMARY KEY,
    name varchar(100) NOT NULL UNIQUE,
    description varchar(255),
    is_active boolean NOT NULL DEFAULT TRUE,
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL
);

CREATE TABLE user_roles (
    user_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id uuid NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE role_permissions (
    role_id uuid NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id uuid NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE refresh_tokens (
    id uuid PRIMARY KEY,
    user_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash varchar(64) NOT NULL UNIQUE,
    family_id uuid NOT NULL,
    expires_at timestamptz NOT NULL,
    revoked_at timestamptz,
    replaced_by uuid,
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL
);
CREATE INDEX idx_refresh_user_id ON refresh_tokens (user_id);
CREATE INDEX idx_refresh_expires_at ON refresh_tokens (expires_at);
CREATE INDEX idx_refresh_family_id ON refresh_tokens (family_id);

CREATE TABLE questionnaires (
    id uuid PRIMARY KEY,
    code varchar(50) NOT NULL UNIQUE,
    title varchar(120) NOT NULL,
    description varchar(500),
    is_default boolean NOT NULL DEFAULT FALSE,
    is_active boolean NOT NULL DEFAULT TRUE,
    version integer NOT NULL DEFAULT 1,
    question_ids jsonb,
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL
);

CREATE TABLE questions (
    id uuid PRIMARY KEY,
    questionnaire_id uuid NOT NULL REFERENCES questionnaires(id) ON DELETE CASCADE,
    question_key varchar(80) NOT NULL,
    text varchar(500) NOT NULL,
    question_type varchar(30) NOT NULL,
    options jsonb,
    question_order integer NOT NULL,
    is_required boolean NOT NULL DEFAULT TRUE,
    is_active boolean NOT NULL DEFAULT TRUE,
    validation_rules jsonb,
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL,
    UNIQUE (questionnaire_id, question_key)
);

CREATE TABLE assessment_sessions (
    id uuid PRIMARY KEY,
    user_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    questionnaire_id uuid REFERENCES questionnaires(id),
    status varchar(30) NOT NULL DEFAULT 'in_progress',
    started_at timestamptz NOT NULL,
    completed_at timestamptz,
    abandoned_at timestamptz,
    current_question_key varchar(80),
    total_questions integer,
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL
);
CREATE INDEX idx_assessment_user_id ON assessment_sessions (user_id);
CREATE INDEX idx_assessment_status ON assessment_sessions (status);
CREATE INDEX idx_assessment_created ON assessment_sessions (created_at);

CREATE TABLE assessment_answers (
    id uuid PRIMARY KEY,
    assessment_id uuid NOT NULL REFERENCES assessment_sessions(id) ON DELETE CASCADE,
    question_id uuid NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    answer_value jsonb NOT NULL,
    answer_source varchar(20) NOT NULL DEFAULT 'user',
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL,
    UNIQUE (assessment_id, question_id)
);
CREATE INDEX idx_answer_assessment ON assessment_answers (assessment_id);

CREATE TABLE health_profiles (
    id uuid PRIMARY KEY,
    assessment_id uuid NOT NULL REFERENCES assessment_sessions(id) ON DELETE CASCADE,
    user_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    profile_data jsonb NOT NULL,
    counsellor_recommended boolean NOT NULL DEFAULT FALSE,
    version integer NOT NULL DEFAULT 1,
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL
);
CREATE INDEX idx_healthprofile_user ON health_profiles (user_id);
CREATE INDEX idx_healthprofile_assessment ON health_profiles (assessment_id);

CREATE TABLE products (
    id uuid PRIMARY KEY,
    name varchar(255) NOT NULL UNIQUE,
    sku varchar(50) NOT NULL,
    category_code varchar(30) NOT NULL,
    category varchar(20) NOT NULL,
    age_group varchar(20) NOT NULL,
    status varchar(20) NOT NULL DEFAULT 'DRAFT',
    description varchar(1000),
    allergens jsonb,
    dietary_tags jsonb,
    nutritional_info jsonb,
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL
);
CREATE INDEX idx_product_status ON products (status);

CREATE TABLE product_rules (
    id uuid PRIMARY KEY,
    product_id uuid NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    rule_definition jsonb NOT NULL,
    is_active boolean NOT NULL DEFAULT TRUE,
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL
);
CREATE INDEX idx_product_rule_product ON product_rules (product_id);

CREATE TABLE recommendations (
    id uuid PRIMARY KEY,
    user_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    assessment_id uuid NOT NULL REFERENCES assessment_sessions(id) ON DELETE CASCADE,
    health_profile_id uuid REFERENCES health_profiles(id) ON DELETE SET NULL,
    product_id uuid NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    recommended_at timestamptz NOT NULL,
    reason varchar(1000),
    confidence double precision NOT NULL,
    rank integer NOT NULL,
    is_active boolean NOT NULL DEFAULT TRUE,
    is_safe boolean NOT NULL DEFAULT TRUE,
    is_excluded boolean NOT NULL DEFAULT FALSE,
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL
);
CREATE INDEX idx_reco_user ON recommendations (user_id);
CREATE INDEX idx_reco_assessment ON recommendations (assessment_id);
CREATE INDEX idx_reco_product ON recommendations (product_id);

CREATE TABLE recommendation_score_history (
    id uuid PRIMARY KEY,
    recommendation_id uuid NOT NULL REFERENCES recommendations(id) ON DELETE CASCADE,
    metric varchar(50) NOT NULL,
    score_value double precision NOT NULL,
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL
);
CREATE INDEX idx_score_history_reco ON recommendation_score_history (recommendation_id);

CREATE TABLE counsellors (
    id uuid PRIMARY KEY,
    user_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    specialty varchar(120) NOT NULL,
    qualification varchar(255),
    is_active boolean NOT NULL DEFAULT TRUE,
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL
);
CREATE INDEX idx_counsellor_user ON counsellors (user_id);

CREATE TABLE user_counsellor_assignments (
    id uuid PRIMARY KEY,
    user_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    counsellor_id uuid NOT NULL REFERENCES counsellors(id) ON DELETE CASCADE,
    assigned_at timestamptz NOT NULL,
    is_active boolean NOT NULL DEFAULT TRUE,
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL
);
CREATE INDEX idx_assignment_user ON user_counsellor_assignments (user_id);
CREATE INDEX idx_assignment_counsellor ON user_counsellor_assignments (counsellor_id);

CREATE TABLE conversations (
    id uuid PRIMARY KEY,
    user_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    counsellor_id uuid REFERENCES counsellors(id) ON DELETE SET NULL,
    status varchar(30) NOT NULL DEFAULT 'active',
    session_type varchar(30) NOT NULL DEFAULT 'welcome',
    context_data jsonb,
    started_at timestamptz NOT NULL,
    ended_at timestamptz,
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL
);
CREATE INDEX idx_conversation_user ON conversations (user_id);
CREATE INDEX idx_conversation_status ON conversations (status);
CREATE INDEX idx_conversation_updated ON conversations (updated_at);

CREATE TABLE messages (
    id uuid PRIMARY KEY,
    conversation_id uuid NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
    role varchar(20) NOT NULL,
    content varchar(20000) NOT NULL,
    message_type varchar(30) NOT NULL DEFAULT 'text',
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL
);
CREATE INDEX idx_message_conversation ON messages (conversation_id);

CREATE TABLE audit_logs (
    id uuid PRIMARY KEY,
    user_id uuid REFERENCES users(id) ON DELETE SET NULL,
    action varchar(50) NOT NULL,
    resource_type varchar(50),
    resource_id uuid,
    metadata jsonb,
    ip_address varchar(45),
    user_agent varchar(255),
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL
);
CREATE INDEX idx_audit_user ON audit_logs (user_id);
CREATE INDEX idx_audit_action ON audit_logs (action);
CREATE INDEX idx_audit_created ON audit_logs (created_at);

CREATE TABLE analytics_events (
    id uuid PRIMARY KEY,
    user_id uuid REFERENCES users(id) ON DELETE SET NULL,
    event_type varchar(80) NOT NULL,
    event_data jsonb,
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL
);
CREATE INDEX idx_analytics_event_type ON analytics_events (event_type);
CREATE INDEX idx_analytics_created ON analytics_events (created_at);

CREATE TABLE ai_configurations (
    id uuid PRIMARY KEY,
    model_name varchar(120) NOT NULL,
    parameters jsonb,
    version integer NOT NULL DEFAULT 1,
    is_active boolean NOT NULL DEFAULT TRUE,
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL
);