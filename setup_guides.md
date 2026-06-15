# Free-Tier Deployment Setup Guides

This document provides step-by-step instructions for deploying the Resume Analysis and Optimisation Engine to the free-tier hosting stack.

---

## 1. Neon PostgreSQL Setup Guide

Neon provides a serverless PostgreSQL database with a generous free tier.

**Steps:**
1. **Account Creation**: Go to [neon.tech](https://neon.tech) and sign up using your GitHub or Google account.
2. **Project Creation**: Click "New Project". 
   - Name: `resume-optimizer-db`
   - Postgres Version: 16
   - Region: Select the region closest to your Render backend (e.g., US East).
3. **Connection Details**: Once created, navigate to the **Dashboard**. Under "Connection Details", select the `JDBC` format.
4. **SSL Configuration**: Ensure the connection string includes `?sslmode=require`. Neon requires SSL for all external connections.
5. **Environment Variables Required**:
   Extract the following from the JDBC URL (`jdbc:postgresql://<user>:<password>@<host>/<dbname>?sslmode=require`):
   - `SPRING_DATASOURCE_URL`: `jdbc:postgresql://<host>/<dbname>?sslmode=require`
   - `SPRING_DATASOURCE_USERNAME`: `<user>`
   - `SPRING_DATASOURCE_PASSWORD`: `<password>`

---

## 2. Cloudinary Setup Guide

Cloudinary manages our resume and PDF document storage efficiently.

**Steps:**
1. **Account Creation**: Go to [cloudinary.com](https://cloudinary.com) and create a free developer account.
2. **Dashboard**: Navigate to the Programmable Media Dashboard.
3. **API Credentials**: Locate your "Account Details" to find:
   - Cloud Name
   - API Key
   - API Secret
4. **Folder Strategy Recommendation**:
   - Create a folder named `resumes` in your Cloudinary Media Library. 
   - The backend is already configured to upload files to this prefix (`resumes/UUID`).
5. **Security**: Do not check "Unsigned Uploads" in upload presets. The backend handles secure, signed uploads using the API Secret.
6. **Environment Variables Required**:
   - `CLOUDINARY_CLOUD_NAME`
   - `CLOUDINARY_API_KEY`
   - `CLOUDINARY_API_SECRET`

---

## 3. Render Deployment Guide (Backend)

Render hosts the Spring Boot Java backend using its Web Service tier.

**Steps:**
1. **Account Setup**: Go to [render.com](https://render.com) and sign in.
2. **Service Creation**: 
   - Click "New +" -> "Web Service".
   - Connect your GitHub repository.
3. **Configuration**:
   - Name: `resume-optimizer-backend`
   - Environment: `Docker`
   - Region: Match your Neon DB region.
   - Root Directory: `backend`
   - Instance Type: Free (or Starter if JVM RAM requirements exceed the free tier).
4. **Environment Variables**:
   Under the "Environment" tab, add:
   - `SPRING_PROFILES_ACTIVE`: `prod`
   - `SPRING_DATASOURCE_URL`: (From Neon)
   - `SPRING_DATASOURCE_USERNAME`: (From Neon)
   - `SPRING_DATASOURCE_PASSWORD`: (From Neon)
   - `JWT_SECRET`: (Generate a secure 256-bit string)
   - `OPENAI_API_KEY`: (Your OpenAI Key)
   - `STORAGE_PROVIDER`: `cloudinary`
   - `CLOUDINARY_CLOUD_NAME`: (From Cloudinary)
   - `CLOUDINARY_API_KEY`: (From Cloudinary)
   - `CLOUDINARY_API_SECRET`: (From Cloudinary)
5. **Health Check Configuration**:
   - Health Check Path: `/actuator/health`
6. **Deploy**: Render will automatically build the Dockerfile located in the `backend/` directory and deploy the container.

---

## 4. Vercel Deployment Guide (Frontend)

Vercel provides seamless Edge Network deployments for Vite React applications.

**Steps:**
1. **Account Setup**: Go to [vercel.com](https://vercel.com) and log in with GitHub.
2. **Project Creation**: 
   - Click "Add New..." -> "Project".
   - Import your repository.
3. **Configuration**:
   - Framework Preset: `Vite`
   - Root Directory: `frontend`
   - Build Command: `npm run build`
   - Output Directory: `dist`
4. **Environment Variables**:
   - `VITE_API_URL`: `https://resume-optimizer-backend.onrender.com/api` (Replace with your actual Render URL).
5. **Deploy**: Click Deploy. Vercel will build the frontend and provide a production URL.
6. **Backend CORS Update**: Remember to update your backend's CORS configuration (if strictly configured) to allow requests from the generated Vercel domain.
