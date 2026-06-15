CREATE TABLE cover_letter_versions (
    id BIGSERIAL PRIMARY KEY,
    cover_letter_id BIGINT NOT NULL REFERENCES cover_letters(id),
    version_number INT NOT NULL,
    version_notes VARCHAR(255),
    generated_content TEXT NOT NULL,
    traceability_json JSONB NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Migrate existing data (if any) to the versions table
INSERT INTO cover_letter_versions (cover_letter_id, version_number, version_notes, generated_content, traceability_json, created_at)
SELECT id, 1, 'Initial version', generated_content, traceability_json, created_at
FROM cover_letters;

-- Drop the content columns from the parent table
ALTER TABLE cover_letters DROP COLUMN generated_content;
ALTER TABLE cover_letters DROP COLUMN traceability_json;
