CREATE TABLE job_applications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    company_name VARCHAR(255) NOT NULL,
    role_title VARCHAR(255) NOT NULL,
    location VARCHAR(255),
    status VARCHAR(50) NOT NULL,
    company_type VARCHAR(50),
    url VARCHAR(1024),
    notes TEXT,
    applied_date DATE,
    resume_id BIGINT REFERENCES resumes(id) ON DELETE SET NULL,
    cover_letter_id BIGINT REFERENCES cover_letters(id) ON DELETE SET NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
