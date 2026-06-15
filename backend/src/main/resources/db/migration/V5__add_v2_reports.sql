ALTER TABLE analyses
ADD COLUMN validation_report_json JSONB,
ADD COLUMN ats_impact_report_json JSONB,
ADD COLUMN original_resume_json JSONB;
