-- Users table
CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    full_name       VARCHAR(255) NOT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Resumes table
CREATE TABLE resumes (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT REFERENCES users(id) ON DELETE CASCADE,
    file_name           VARCHAR(255) NOT NULL,
    file_type           VARCHAR(10) NOT NULL,      -- PDF, DOCX
    file_content        BYTEA NOT NULL,            -- Replaced s3_key with file_content
    raw_text            TEXT,
    professional_summary TEXT,
    experience_json     JSONB,                     -- Structured experience data
    virtual_experience_json JSONB,                 -- Virtual experience data
    skills_json         JSONB,                     -- Skills array
    education_json      JSONB,                     -- Education array
    projects_json       JSONB,                     -- Projects array
    certifications_json JSONB,                     -- Certifications array
    achievements_json   JSONB,                     -- Achievements array
    parsed              BOOLEAN DEFAULT FALSE,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Job Descriptions table
CREATE TABLE job_descriptions (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT REFERENCES users(id) ON DELETE CASCADE,
    title               VARCHAR(255),
    company             VARCHAR(255),
    source_type         VARCHAR(10) NOT NULL,      -- PDF, DOCX, TEXT
    file_name           VARCHAR(255),
    file_content        BYTEA,
    raw_text            TEXT NOT NULL,
    required_skills_json    JSONB,
    preferred_skills_json   JSONB,
    technologies_json       JSONB,
    responsibilities_json   JSONB,
    keywords_json           JSONB,
    domain_knowledge_json   JSONB,
    parsed              BOOLEAN DEFAULT FALSE,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Analyses table (ATS + Recruiter combined)
CREATE TABLE analyses (
    id                  BIGSERIAL PRIMARY KEY,
    resume_id           BIGINT REFERENCES resumes(id) ON DELETE CASCADE,
    job_description_id  BIGINT REFERENCES job_descriptions(id) ON DELETE CASCADE,
    -- ATS Results
    ats_score           INTEGER,                    -- 0-100
    missing_technical_keywords  JSONB,
    missing_soft_skills         JSONB,
    missing_domain_keywords     JSONB,
    missing_certifications      JSONB,
    missing_tooling             JSONB,
    ats_raw_response            TEXT,               -- Raw AI response
    -- Recruiter Results
    recruiter_fit_score         INTEGER,            -- 0-100
    shortlisting_probability    VARCHAR(20),        -- HIGH, MEDIUM, LOW
    strengths_json              JSONB,
    weaknesses_json             JSONB,
    improvement_suggestions_json JSONB,
    recruiter_raw_response      TEXT,               -- Raw AI response
    -- Metadata
    analysis_status     VARCHAR(20) DEFAULT 'PENDING',
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tailored Resumes table
CREATE TABLE tailored_resumes (
    id                  BIGSERIAL PRIMARY KEY,
    analysis_id         BIGINT REFERENCES analyses(id) ON DELETE CASCADE,
    resume_type         VARCHAR(50) NOT NULL,       -- GENERAL, BACKEND, FINTECH, PRODUCT
    content_json        JSONB NOT NULL,             -- Tailored resume content
    changes_summary     TEXT,                       -- What was changed and why
    generated_s3_key    VARCHAR(500),               -- Path to generated PDF/DOCX in S3
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Company Matches table
CREATE TABLE company_matches (
    id                      BIGSERIAL PRIMARY KEY,
    analysis_id             BIGINT REFERENCES analyses(id) ON DELETE CASCADE,
    company_name            VARCHAR(255) NOT NULL,
    match_score             INTEGER,                -- 0-100
    missing_skills_json     JSONB,
    recruiter_notes         TEXT,
    sponsorship_likelihood  VARCHAR(10),            -- HIGH, MEDIUM, LOW
    interview_difficulty    VARCHAR(20),            -- EASY, MEDIUM, HARD, VERY_HARD
    created_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Workflow Runs table (tracks n8n workflow executions)
CREATE TABLE workflow_runs (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT REFERENCES users(id) ON DELETE CASCADE,
    resume_id           BIGINT REFERENCES resumes(id),
    job_description_id  BIGINT REFERENCES job_descriptions(id),
    n8n_execution_id    VARCHAR(255),
    workflow_status     VARCHAR(20) DEFAULT 'STARTED',  -- STARTED, RUNNING, COMPLETED, FAILED
    current_step        VARCHAR(100),
    steps_completed     JSONB,
    error_message       TEXT,
    started_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at        TIMESTAMP
);

-- Indexes
CREATE INDEX idx_resumes_user_id ON resumes(user_id);
CREATE INDEX idx_job_descriptions_user_id ON job_descriptions(user_id);
CREATE INDEX idx_analyses_resume_id ON analyses(resume_id);
CREATE INDEX idx_analyses_job_desc_id ON analyses(job_description_id);
CREATE INDEX idx_tailored_resumes_analysis_id ON tailored_resumes(analysis_id);
CREATE INDEX idx_company_matches_analysis_id ON company_matches(analysis_id);
CREATE INDEX idx_workflow_runs_user_id ON workflow_runs(user_id);
