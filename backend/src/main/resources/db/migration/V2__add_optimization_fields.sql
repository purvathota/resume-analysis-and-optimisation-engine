ALTER TABLE analyses
ADD COLUMN optimized_resume_json jsonb,
ADD COLUMN keyword_improvement_report_json jsonb;
