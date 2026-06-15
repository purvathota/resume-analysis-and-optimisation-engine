# Environment Variable Checklist

Before deploying the application, ensure all the following environment variables are securely configured in your hosting environments (Render for Backend, Vercel for Frontend).

## 1. Backend (Render)

### Database Configuration (Neon PostgreSQL)
- `SPRING_DATASOURCE_URL`: The JDBC connection string (e.g., `jdbc:postgresql://<neon-host>/<db>?sslmode=require`)
- `SPRING_DATASOURCE_USERNAME`: The database user.
- `SPRING_DATASOURCE_PASSWORD`: The database password.

### Security & Authentication
- `JWT_SECRET`: A secure, randomly generated 256-bit base64 string used to sign JSON Web Tokens.

### External APIs
- `OPENAI_API_KEY`: Your OpenAI API key for AI generation features.

### Storage Configuration (Cloudinary)
- `STORAGE_PROVIDER`: Must be set to `cloudinary` (default fallback).
- `CLOUDINARY_CLOUD_NAME`: Your Cloudinary Cloud Name.
- `CLOUDINARY_API_KEY`: Your Cloudinary API Key.
- `CLOUDINARY_API_SECRET`: Your Cloudinary API Secret.

### Spring Profiles
- `SPRING_PROFILES_ACTIVE`: Must be set to `prod` to load `application-prod.yml`.

---

## 2. Frontend (Vercel)

### API Connection
- `VITE_API_URL`: The absolute URL of your backend's API endpoint. 
  - *Example*: `https://resume-optimizer-backend.onrender.com/api`
  - Ensure no trailing slashes.

---

## 3. Local Development (Docker Compose)
If running locally via `.env`, ensure the following are populated:
- `DB_USER`
- `DB_PASSWORD`
- `JWT_SECRET`
- `OPENAI_API_KEY`
- `CLOUDINARY_CLOUD_NAME`
- `CLOUDINARY_API_KEY`
- `CLOUDINARY_API_SECRET`
- `STORAGE_PROVIDER=cloudinary`
