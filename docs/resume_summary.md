# 📄 Resume & LinkedIn Portfolio Summaries

Use these snippets to add the project to your resume, LinkedIn profile or personal portfolio website.

---

## 1-Line Project Summary
A production-grade AI platform built with React and Spring Boot that optimizes resumes and tracks applications through traceability-driven validation and workflow automation.

---

## 3-Line Project Summary
An enterprise-grade Resume Analysis and Optimisation Engine built with Java 21, Spring Boot 3.5, and React 19. It solves the critical flaw of LLM hallucination in resume generation by introducing a 6-domain Traceability Guard that mechanically validates AI output against source documents before allowing PDF/DOCX export. The platform also includes an immutable 1:N versioned cover letter generator and an integrated application tracker, fully deployed on AWS with Prometheus and Grafana observability.

---

## Resume Bullet Points (Action-Oriented)

* **Created a full-stack AI Resume Optimizer** using Java 21, Spring Boot 3.5, and React 19, integrating OpenAI (GPT-4o) to tailor resumes while strictly enforcing factual accuracy.
* **Designed a custom deterministic Traceability Guard** that validates AI-generated outputs across 6 domains (experience, metrics, technologies), preventing unsupported AI-generated content and ensuring recruiter-grade data integrity.
* **Established an immutable 1:N document versioning system** for cover letters, utilizing PostgreSQL JSONB columns and a React Diff Viewer to allow users to visually track iterative prompt changes.
* **Integrated a Job Application Tracker** tightly coupled with the document generation pipeline, enabling users to track application progress and outcomes tied to specific resume and cover letter versions.
* **Deployed a highly observable cloud infrastructure** on AWS (EC2, RDS, S3) using Docker and GitHub Actions, instrumenting the backend with Micrometer, Prometheus, and Grafana to monitor JVM metrics and HikariCP connection pools.
* **Secured the platform** using stateless JWT authentication, Spring Security, and AWS IAM roles for private S3 document storage, intercepting all requests with custom MDC filters for structured correlation ID logging.

---

## LinkedIn Project Description

**Resume Analysis and Optimisation Engine | Java, Spring Boot, React, AWS**

I recently built a full-stack AI platform designed to solve the biggest problem with AI resume builders: **Hallucination**. Recruiters need factual accuracy, so I built a "95% Truthfulness, 5% Tailoring" engine. 

Instead of blindly trusting GPT-4o, the Spring Boot backend runs all AI output through a strict 6-domain Traceability Guard. If the AI invents a metric or technology, the export is mechanically blocked. 

**Key Engineering Highlights:**
🔹 **Backend:** Java 21, Spring Boot 3.5, PostgreSQL, Spring Security, JWT
🔹 **Frontend:** React 19, TypeScript, Tailwind CSS, Vite
🔹 **Cloud & DevOps:** AWS (EC2, RDS, S3), Docker, GitHub Actions (CI/CD)
🔹 **Observability:** Prometheus, Grafana, Micrometer, structured logging with correlation IDs
🔹 **Features:** ATS Gap Analysis, immutable 1:N versioned cover letters (with Diff Viewer), and an integrated job application tracker.

Check out the full repository and architecture diagrams here: [Link to GitHub]
