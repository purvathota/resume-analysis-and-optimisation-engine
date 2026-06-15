-- Rename S3 Object Key to a generic Storage Reference
ALTER TABLE resumes RENAME COLUMN s3_object_key TO storage_reference;
