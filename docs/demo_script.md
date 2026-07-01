# 🎬 Demo Video Script (5-7 Minutes)

**Target Audience:** Technical Recruiters, Hiring Managers, Senior Engineers

## 1. Introduction (0:00 - 0:30)
* *"Hi, I'm [Your Name], and this is the Resume Analysis and Optimisation Engine."*
* *"Most AI resume tools on the market hallucinate—they invent experiences to hit 100% ATS matches. Recruiters hate this. I built this platform around a '95% Truthfulness, 5% Tailoring' philosophy, using a custom deterministic Traceability Guard to ensure strict traceability-based validation."*
* *"The stack is React 19, Java 21, Spring Boot 3.5, and PostgreSQL, deployed on modern cloud infrastructure. Let's dive in."*

## 2. Authentication & Dashboard (0:30 - 1:00)
* **Action:** Log in and show the Dashboard.
* *"The platform is secured via stateless JWT authentication. Once logged in, you see the Dashboard, giving a high-level view of your application pipeline."*

## 3. Resume Upload & ATS Analysis (1:00 - 2:00)
* **Action:** Upload a PDF resume and paste a Job Description.
* *"Here, I upload my base PDF resume and target Job Description. The backend uses PDFBox to parse the text, stores the original PDF in cloud storage (Cloudinary), and then performs an initial ATS keyword gap analysis."*
* *"You can see the keyword density comparison immediately, identifying exactly what the AI needs to focus on."*

## 4. Resume Optimisation & Validation Engine (2:00 - 3:30)
* **Action:** Click 'Optimize' and show the result.
* *"Now for the core feature. When I hit optimize, the backend sends the data to GPT-4o with strict deterministic prompts. But here's the crucial part: the backend doesn't just blindly trust the AI."*
* *"It runs the output through a 6-domain Traceability Validation Engine. If the AI hallucinates a metric or a technology I don't actually have, the backend rejects the export. Because this passed, we can safely export the tailored PDF."*

## 5. Cover Letter Generation & Versioning (3:30 - 4:30)
* **Action:** Go to Cover Letter Generator, generate one, and open Version History.
* *"Next is the Cover Letter Generator. Instead of overwriting files, I built an immutable 1:N versioning system. Every time you generate a letter for a specific company and role, it creates a new version."*
* *"Using the integrated Diff Viewer, you can visually compare Version 1 to Version 2 to see exactly how the AI tailored the tone."*

## 6. Job Tracker Integration (4:30 - 5:15)
* **Action:** Click "Create Application from Cover Letter" and navigate to Job Tracker.
* *"Instead of a disconnected workflow, I integrated a Job Application Tracker. By clicking 'Create Application' right from the cover letter, it automatically drops it into my pipeline."*
* *"Every application is strictly linked to the exact resume and cover letter version used, allowing you to track conversion rates scientifically."*

## 7. Monitoring, CI/CD, & Outro (5:15 - 6:30)
* **Action:** Open Grafana dashboard showing JVM metrics.
* *"Finally, this is built for production. The backend uses Spring Boot Actuator and Micrometer to expose metrics to Prometheus."*
* *"In Grafana, we can monitor JVM Garbage Collection, HikariCP database connection pools, and custom business metrics like total resumes generated."*
* *"Everything is containerized using Docker and deployed to production via a GitHub Actions CI/CD pipeline."*
* *"Thanks for watching!"*

---

# 📸 Final Screenshot Checklist

Make sure you capture exactly these images and save them in your `docs/images/` directory:

- [ ] `login.png` - The user authentication login page.
- [ ] `register.png` - The new user registration page.
- [ ] `dashboard.png` - The main logged-in dashboard view.
- [ ] `resume-upload.png` - The file upload and JD pasting screen.
- [ ] `ats-analysis.png` - The keyword gap detection radar/bar chart.
- [ ] `optimized-resume.png` - The side-by-side view of the optimized resume.
- [ ] `validation-report.png` - The UI showing the Traceability Guard passing/failing.
- [ ] `cover-letter-workflow.png` - The main cover letter generation screen with version history.
- [ ] `diff-viewer.png` - The side-by-side Resume Traceability Diff View comparing original master resume bullets against optimized tailored bullets.
- [ ] `job-tracker-add-application.png` - The Job Tracker add application modal view.
- [ ] `job-tracker-edit-application.png` - The Job Tracker edit application modal view.
- [ ] `swagger.png` - The Swagger API UI documentation.

- [ ] `system-architecture.png` - High-level system architecture overview (diagram).
- [ ] `security-architecture.png` - Authentication and security flow overview (diagram).
