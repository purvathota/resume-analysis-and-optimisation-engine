<div align="center">
  <h1>Resume Analysis and Optimisation Engine</h1>
  <p>A production-grade platform for resume optimisation, AI-assisted document generation, workflow automation, and application lifecycle tracking.</p>

  ![Java](https://img.shields.io/badge/Java_21-ED8B00?style=for-the-badge&logo=java&logoColor=white)
  ![Spring Boot](https://img.shields.io/badge/Spring_Boot_3.5-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
  ![React](https://img.shields.io/badge/React_19-20232A?style=for-the-badge&logo=react&logoColor=61DAFB)
  ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)
  ![Vercel](https://img.shields.io/badge/Vercel-000000?style=for-the-badge&logo=vercel&logoColor=white)
  ![OpenAI](https://img.shields.io/badge/GPT--4o-412991?style=for-the-badge&logo=openai&logoColor=white)
</div>

---

## 🌐 Live Demo & Documentation

| Resource | Link |
| :--- | :--- |
| **Live Application** | [https://resume-analysis-and-optimisation-en.vercel.app](https://resume-analysis-and-optimisation-en.vercel.app) |
| **Swagger API Docs** | [https://resume-analysis-and-optimisation-engine.onrender.com/swagger-ui.html](https://resume-analysis-and-optimisation-engine.onrender.com/swagger-ui.html) |
| **Backend API Base** | [https://resume-analysis-and-optimisation-engine.onrender.com](https://resume-analysis-and-optimisation-engine.onrender.com) |

---

## 📸 Screenshots & Demo

| Login Screen | Registration Screen |
| :---: | :---: |
| ![Login](docs/images/login.png) | ![Register](docs/images/register.png) |

| Dashboard | Resume Upload & JD | ATS Analysis |
| :---: | :---: | :---: |
| ![Dashboard](docs/images/dashboard.png) | ![Resume Upload](docs/images/resume-workflow.png) | ![ATS Analysis](docs/images/ats-analysis.png) |

| Validation Report | Diff Viewer | Optimized Resume
| :---: | :---: | :---: |
| ![Validation Report](docs/images/validation-report.png) | ![Diff Viewer](docs/images/diff-viewer.png) | ![Resume Optimization](docs/images/optimized-resume.png) 

| Cover Letter Generator | Version History | Application Saved |
| :---: | :---: | :---: |
| ![Cover Letter](docs/images/cover-letter-workflow.png) | ![History](docs/images/history.png) | ![Application Saved](docs/images/job-tracker-application-saved.png) |

| Analytics & Job Stats (in Tracker) | Swagger API Docs |
| :---: | :---: |
| ![Job Tracker](docs/images/job-tracker-workflow.png) | ![Add Application](docs/images/job-tracker-add-application.png) | ![Edit Application](docs/images/job-tracker-edit-application.png) | ![Swagger UI](docs/images/swagger.png) |

---

## 💡 Why I Built This

Most AI resume optimization tools on the market suffer from a fatal flaw: **Hallucination**. They invent experiences, fabricate metrics and hallucinate technologies to achieve a 100% ATS keyword match. 

**Recruiters and Engineering Managers care about factual accuracy above all else.**

I built this platform around a philosophy of **"95% Truthfulness, 5% Tailoring"**. To enforce this mechanically, I engineered a **Traceability Guard**—a strict backend validation pipeline that forces the AI (GPT-4o) to map every single generated bullet point directly back to the original source resume. If the system detects fabricated data, the optimization is rejected and blocked from export. 

This project aims to provide *trustworthy* AI-assisted optimization, not blind AI rewriting.

---

## 🚀 Platform Capabilities

- ✓ **6-domain AI traceability validation engine**
- ✓ **ATS analysis and keyword gap detection**
- ✓ **Resume optimisation with integrity validation**
- ✓ **Versioned cover letter management**
- ✓ **Diff-based version comparison**
- ✓ **Application tracking and analytics**
- ✓ **GitHub Actions CI/CD pipeline**
- ✓ **Prometheus & Grafana observability**
- ✓ **PDF and DOCX export**

---

## 🏗️ Architecture Summary

| Layer | Technologies |
| --- | --- |
| **Frontend** | React 19, TypeScript, Tailwind CSS, Vite |
| **Backend** | Java 21, Spring Boot 3.5 (Web, Security, Data JPA, Validation) |
| **Database** | PostgreSQL 16, Flyway (Migrations) |
| **AI** | OpenAI GPT-4o API |
| **Workflow** | n8n |
| **Monitoring** | Prometheus, Grafana, Micrometer, Spring Boot Actuator |
| **Security** | JWT (HS256), Spring Security |

## 🛠️ Engineering Highlights

- **Stateless JWT Authentication** for decoupled frontend-backend communication.
- **PostgreSQL JSONB Modelling** to store unstructured AI generation history alongside structured relational data.
- **OpenAI GPT-4o Integration** using deterministic prompt engineering for ATS optimization.
- **n8n Workflow Orchestration** for asynchronous webhook-based automation.
- **Versioned Document Generation** utilizing a 1:N relational model and React Diff Viewer.
- **Structured Logging** with injected Correlation UUIDs via custom MDC servlet filters.
- **Prometheus & Grafana Monitoring** exposing Actuator and Micrometer metrics for JVM, HTTP, and connection pools.
- **GitHub Actions CI/CD** automating builds and testing prior to deployment.
- **Cloud Storage Abstraction** supporting Cloudinary via a `StorageService` interface.

---

## ⚙️ Engineering Decisions

Building a production-ready platform requires intentional trade-offs. Here is the rationale behind my core architectural choices:

1. **Why PostgreSQL + JSONB?**
   While MongoDB is popular for document generation, I chose PostgreSQL. Resumes and Applications are highly relational, but the generated AI responses (Traceability Data, Gap Analysis) are unstructured. PostgreSQL's JSONB columns gave me the strict ACID compliance needed for user relationships, with the schema flexibility needed for evolving LLM JSON outputs.
2. **Why Cloud Storage instead of Database BLOB storage?**
   Storing raw PDFs as `bytea` in PostgreSQL bloats the database size rapidly, hurting database cache hit ratios and increasing database costs. Offloading binary files to cloud document storage (Cloudinary) keeps the database lean and fast.
3. **Why Versioned Cover Letters?**
   Initial user testing revealed that users wanted to tweak cover letters without losing previous generations. Instead of overwriting (1:1), I implemented a 1:N relational model (`CoverLetter` -> `CoverLetterVersion`) with a React Diff Viewer so users can track changes iteratively.
4. **Why Prometheus + Grafana over CloudWatch?**
   While CloudWatch is easy to set up, integrating Micrometer with Prometheus allowed me to expose deep JVM metrics (Garbage Collection pauses) and HikariCP database connection pool metrics locally during development, ensuring the app was hardened *before* cloud deployment.
5. **Why JWT Authentication?**
   To support a decoupled React frontend and potential future mobile clients, stateful session cookies were avoided. JWTs provide a stateless, easily scalable security model, validated via a custom Spring Security filter on every request.

---

## 🗺️ System Workflows & Architecture

### System Architecture
The application follows a modular monolith architecture with clearly separated frontend, backend, workflow, storage, and AI integration layers.
![System Architecture](docs/images/system-architecture.png)

### 1. Resume Optimization Workflow
The core feature involves strict deterministic validation against the source document.
![Resume Workflow](docs/images/resume-workflow-architecture.png)

### 2. Cover Letter Workflow
Every generation creates an immutable snapshot.
![Cover Letter Workflow](docs/images/cover-letter-workflow-architecture.png)

### 3. Job Tracker Workflow
Tight integration allows 1-click application tracking right from the document generation screens.
![Job Tracker Workflow](docs/images/job-tracker-workflow-architecture.png)

---

## 📊 Observability & Monitoring

The platform is instrumented for "Day 2" operations using Spring Boot Actuator, Micrometer, Prometheus, and Grafana.

- **Business Metrics**: Custom `Counter` beans track `resume.generations.count` and `jobapplication.creations.count`.
- **System Metrics**: Automatically exports JVM Memory, GC times, HTTP request latency percentiles, and HikariCP connection pool health.
- **Structured Logging**: A custom `RequestLoggingFilter` injects a unique UUID (`requestId`) into the MDC (Mapped Diagnostic Context) for every request, allowing easy cross-referencing in log aggregators.
- **Health Indicators**: Custom `HealthIndicator` beans actively poll cloud storage, OpenAI, and n8n to provide real-time dependency status on the `/health` endpoint.

---

## 🛡️ Security Architecture

Security is built-in at the framework layer using Spring Security.

- **Authentication**: Stateless HS256 signed JWT tokens.
- **Authorization**: Hardened ownership verification ensures `User A` cannot query the database for `User B`'s documents (IDOR protection).
- **Data Validation**: Strict `@Valid` Jakarta bean validation on all incoming REST payloads.
- **Global Error Handling**: A `@ControllerAdvice` global exception handler standardizes all errors into clean JSON, ensuring internal stack traces never leak to the frontend.

![Security Architecture](docs/images/security-architecture.png)

---

## ☁️ Deployment Architecture

The application is built with a flexible, provider-agnostic deployment strategy in mind. 

### Live Hosting Environment
For the live portfolio demonstration, the application is deployed on a modern cloud stack:
- **Frontend Hosting**: Vercel (Auto-deploy via GitHub Actions).
- **Backend Hosting**: Render Web Service (Spring Boot 3.5).
- **Database**: Neon PostgreSQL (Serverless PostgreSQL).
- **File Storage**: Cloudinary (via `CloudinaryStorageService`).
- **Observability**: Render Logs, Actuator, Prometheus, and Grafana.

This dual-target capability is achieved by an abstract `StorageService` interface and Spring Boot Profiles, allowing seamless switching between environments without altering domain logic.

---

## 🔌 API Overview

The backend exposes a comprehensive RESTful API. For the complete OpenAPI specification and interactive testing environment, visit `/swagger-ui.html`.

| Module | Responsibility | Example Endpoints |
|----------|----------|----------|
| **Authentication** | User registration, login, JWT issuance | `/api/auth/register`, `/api/auth/login` |
| **Resume Management** | Resume upload, parsing, storage, export | `/api/resumes/upload`, `/api/resumes/{id}/export/pdf` |
| **Resume Analysis** | ATS scoring, keyword gap analysis, optimisation workflows | `/api/resumes/{id}/analyze`, `/api/resumes/{id}/optimize` |
| **Cover Letter Management** | Cover letter generation, version history, exports | `/api/cover-letters`, `/api/cover-letters/{id}/versions` |
| **Job Tracker** | Application lifecycle tracking, status updates, analytics | `/api/job-applications` |
| **Monitoring & Health** | Platform health checks and metrics | `/actuator/health`, `/actuator/prometheus` |

### API Documentation

Swagger UI is available at:

```text
/swagger-ui.html
---

## 💻 Local Development Setup

### Prerequisites
- Java 21
- Node.js 20+
- Docker & Docker Compose
- Cloudinary Account (API Key & Secret)
- OpenAI API Key

### Running Locally
1. **Clone the repo**
   ```bash
   git clone https://github.com/yourusername/ai-resume-optimizer.git
   ```
2. **Environment Variables**
   Create a `.env` file in the root directory:
   ```env
   POSTGRES_USER=resumeoptimizer
   POSTGRES_PASSWORD=your_password
   JWT_SECRET=your_super_secret_jwt_key
   OPENAI_API_KEY=sk-your-key
   ```
3. **Boot Infrastructure** (PostgreSQL, n8n, MailHog)
   ```bash
   docker compose up -d
   ```
4. **Start Backend**
   ```bash
   cd backend
   ./mvnw spring-boot:run
   ```
5. **Start Frontend**
   ```bash
   cd frontend
   npm install
   npm run dev
   ```

---

## 📈 Lessons Learned

1. **AI Validation & Prompt Engineering**: I learned that you cannot trust an LLM to police itself. Even with zero-temperature prompting, GPT-4o will occasionally hallucinate to hit 100% ATS matches. Building a backend validation engine (the Traceability Guard) taught me how to strictly enforce deterministic outcomes on non-deterministic models.
2. **Versioning Strategies**: Moving from a simple 1:1 Cover Letter model to an immutable 1:N Versioning system taught me how to manage complex Hibernate cascade types and lazy loading without impacting performance.
3. **Cloud Native Storage**: Offloading binary documents to cloud document storage (Cloudinary) instead of using PostgreSQL `bytea` columns kept database query latencies low and reduced database storage costs.
4. **Observability**: Adding Micrometer and Grafana wasn't just "for show." It directly helped me debug slow database queries during load testing by surfacing HikariCP pool exhaustion—a problem I wouldn't have caught with basic console logs.
5. **Security & JWTs**: Implementing stateless JWT authentication with Spring Security and custom MDC logging filters taught me how to build secure, auditable APIs ready for enterprise deployment.
6. **Workflow Automation**: Integrating n8n via webhooks provided hands-on experience orchestrating external asynchronous workflows outside the core Spring Boot application context.

---

## 🎯 Project Outcomes

This project demonstrates practical experience across:

- Backend Engineering
- Cloud Engineering
- AI Integration
- Workflow Automation
- Security Engineering
- Observability
- DevOps & CI/CD
- System Design

The platform was intentionally designed to resemble a production-grade engineering system rather than a proof-of-concept AI application.

---

## 🔮 Future Enhancements

- Redis caching layer for frequently requested ATS analyses
- Event-driven document processing using Kafka
- Real-time progress updates via Server-Sent Events (SSE)
- AI-assisted interview preparation workflows
- Advanced recruiter and application analytics

## 📄 License
This project is licensed under the MIT License - see the LICENSE file for details.

---

## ✉️ Contact Information

**Purva Thota**
- 💼 [LinkedIn](https://www.linkedin.com/in/purva5/)
- 🌐 [Portfolio](https://yourportfolio.com)
- 📧 [Email](mailto:[purvathota@gmail.com])
