ALTER TABLE resumes
DROP COLUMN file_content,
ADD COLUMN s3_object_key VARCHAR(500);
