CREATE TABLE cover_letters (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    resume_id BIGINT NOT NULL REFERENCES resumes(id),
    job_description_id BIGINT REFERENCES job_descriptions(id),
    company_name VARCHAR(255) NOT NULL,
    role_title VARCHAR(255) NOT NULL,
    generated_content TEXT NOT NULL,
    traceability_json JSONB NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
