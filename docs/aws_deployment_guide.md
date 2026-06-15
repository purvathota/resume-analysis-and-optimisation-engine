# Phase 2.1: AWS Production Deployment Guide

This guide details the complete process for deploying the AI Resume Optimizer in a production environment on AWS, incorporating Spring Boot, React, n8n, RDS, S3, ECR, CloudWatch, and Nginx.

## Architecture

```mermaid
graph TD
    User([User]) -->|HTTPS (443)| Nginx[Nginx Reverse Proxy\nAWS EC2]
    
    subgraph AWS EC2 Instance
        Nginx -->|Port 80| Frontend[React Frontend\nDocker Container]
        Nginx -->|Port 8080| Backend[Spring Boot Backend\nDocker Container]
        Nginx -->|Port 5678| n8n[n8n Workflow Engine\nDocker Container]
    end
    
    Backend <-->|JDBC| RDS[(AWS RDS\nPostgreSQL)]
    Backend <-->|AWS SDK| S3[(AWS S3\nDocument Storage)]
    Backend <-->|API| OpenAI[OpenAI API]
    
    Backend -->|awslogs| CW[CloudWatch Logs]
    Frontend -->|awslogs| CW
    n8n -->|awslogs| CW
    Nginx -->|awslogs| CW
    
    subgraph CI/CD
        GH[GitHub Actions] -.->|Build & Push| ECR[Amazon ECR]
        GH -.->|SSH Deploy & Pull| Nginx
    end
```

## Step 1: AWS Resource Provisioning

### 1.1 AWS RDS (PostgreSQL)
1. Go to AWS RDS Console and click **Create database**.
2. Select **PostgreSQL** (version 16 recommended).
3. Select **Free tier** or **Production** based on your budget.
4. Set Master Username (e.g., `resumeoptimizer`) and Master Password.
5. Under **Connectivity**, set **Public access** to **No** (ensure your EC2 instance is in the same VPC).
6. Create database and note the **Endpoint URL**.

### 1.2 AWS S3 Bucket
1. Go to AWS S3 Console and click **Create bucket**.
2. Name it (e.g., `resume-optimizer-storage-prod`).
3. **Block all public access** (Files are securely fetched by the backend using AWS SDK).
4. Create bucket.

### 1.3 Amazon ECR
1. Go to Amazon ECR Console.
2. Create two private repositories:
   - `resume-optimizer-backend`
   - `resume-optimizer-frontend`

### 1.4 IAM User for GitHub Actions & Backend
1. Create a new IAM User named `github-actions-deploy`.
2. Attach policies:
   - `AmazonEC2ContainerRegistryPowerUser` (for pushing to ECR)
   - `AmazonS3FullAccess` (or scoped policy for your bucket)
   - `CloudWatchLogsFullAccess` (for creating log groups)
3. Generate **Access Key ID** and **Secret Access Key**.

## Step 2: EC2 Setup

1. Launch an EC2 instance (Ubuntu 24.04 LTS recommended, t3.medium or larger).
2. Configure Security Group:
   - Allow SSH (Port 22) from your IP.
   - Allow HTTP (Port 80) and HTTPS (Port 443) from anywhere.
3. SSH into the instance and install Docker and Docker Compose:
   ```bash
   sudo apt update
   sudo apt install docker.io docker-compose-v2 awscli -y
   sudo usermod -aG docker $USER
   ```
   *Logout and login again for group changes to take effect.*
4. Create the application directory:
   ```bash
   mkdir -p ~/resume-optimizer/nginx
   ```

## Step 3: GitHub Actions Secrets

In your GitHub repository, navigate to **Settings > Secrets and variables > Actions**, and add the following secrets:

- `AWS_ACCESS_KEY_ID`: From IAM User
- `AWS_SECRET_ACCESS_KEY`: From IAM User
- `AWS_ACCOUNT_ID`: Your 12-digit AWS Account ID
- `EC2_HOST`: Public IP or DNS of your EC2 instance
- `EC2_USERNAME`: `ubuntu` (if using Ubuntu AMI)
- `EC2_SSH_KEY`: Your private `.pem` key content

## Step 4: Application Environment Setup

On the EC2 instance, create a `.env` file in `~/resume-optimizer`:
```bash
nano ~/resume-optimizer/.env
```
Populate it with your production secrets:
```env
DOMAIN_NAME=yourdomain.com
N8N_BASIC_AUTH_USER=admin
N8N_BASIC_AUTH_PASSWORD=securepassword

RDS_URL=jdbc:postgresql://your-rds-endpoint.amazonaws.com:5432/resumeoptimizer
RDS_USERNAME=resumeoptimizer
RDS_PASSWORD=your_rds_password

JWT_SECRET=a_very_long_secure_random_string_for_jwt_signature
OPENAI_API_KEY=sk-proj-...

AWS_S3_BUCKET=resume-optimizer-storage-prod
AWS_REGION=us-east-1
AWS_ACCESS_KEY_ID=...
AWS_SECRET_ACCESS_KEY=...
```

## Step 5: HTTPS via Certbot (Optional but Recommended)

Once the application is running via GitHub Actions:
1. SSH into the EC2 instance.
2. Install Certbot: `sudo apt install certbot -y`
3. Run: `sudo certbot certonly --webroot -w ~/resume-optimizer/certbot -d yourdomain.com`
4. Uncomment the HTTPS section in `~/resume-optimizer/nginx/nginx.conf` and run `docker compose -f docker-compose.prod.yml restart nginx`.

## Verification

- **Actuator Health Check:** Navigate to `https://yourdomain.com/api/actuator/health` to verify the backend and DB connection.
- **S3 Upload:** Upload a resume via the frontend. Check your S3 bucket to verify the file was stored securely.
- **CloudWatch Logs:** Navigate to AWS CloudWatch Logs. Verify that log groups for `/ec2/resume-optimizer-backend`, `frontend`, `nginx`, and `n8n` are successfully capturing application output.
