# ChargeFlow Public Deployment Guide

This guide outlines the steps to deploy the **ChargeFlow** full-stack application (Spring Boot Backend + MySQL Database + React Vite Frontend) to the public cloud so that anyone can access it.

---

## Architecture Overview

For a public deployment, the application components must be hosted as follows:
```
                                     +---------------------------+
                                     |    React Vite Frontend    |
                                     |    (Vercel or Netlify)    |
                                     +-------------+-------------+
                                                   |
                                                   | HTTP API Requests
                                                   v
                                     +-------------+-------------+
                                     |   Spring Boot REST API    |
                                     |     (Render or Railway)   |
                                     +-------------+-------------+
                                                   |
                                                   | JDBC Connection
                                                   v
                                     +-------------+-------------+
                                     |       MySQL Database      |
                                     |     (Railway or AWS RDS)  |
                                     +---------------------------+
```

---

## Option 1: PaaS Deployment (Recommended & Easiest)

This approach uses managed serverless platforms (Render, Vercel, and Railway) which have generous free tiers.

### Step 1: Deploy the MySQL Database (on Railway)
1. Go to [Railway.app](https://railway.app/) and sign up.
2. Click **New Project** -> **Provision MySQL**.
3. Once initialized, click on the **MySQL** card, switch to the **Variables** tab, and copy the connection details:
   * `MYSQLHOST`
   * `MYSQLPORT`
   * `MYSQLUSER`
   * `MYSQLPASSWORD`
   * `MYSQLDATABASE`

### Step 2: Deploy the Backend API (on Render)
1. Sign up at [Render.com](https://render.com/).
2. Click **New** -> **Web Service**.
3. Connect your GitHub repository.
4. Set the following details:
   * **Name:** `chargeflow-backend`
   * **Root Directory:** `backend`
   * **Runtime:** `Docker` (Render will automatically detect the `Dockerfile` inside `/backend`)
5. Click **Advanced** and add the following **Environment Variables**:
   * `SPRING_DATASOURCE_URL` = `jdbc:mysql://<MYSQLHOST>:<MYSQLPORT>/<MYSQLDATABASE>?useSSL=false&allowPublicKeyRetrieval=true`
   * `SPRING_DATASOURCE_USERNAME` = `<MYSQLUSER>`
   * `SPRING_DATASOURCE_PASSWORD` = `<MYSQLPASSWORD>`
6. Click **Deploy Web Service**. Render will build and deploy your Spring Boot JAR. Take note of your public backend URL (e.g. `https://chargeflow-backend.onrender.com`).

### Step 3: Deploy the Frontend (on Vercel)
1. Go to [Vercel.com](https://vercel.com/) and link your GitHub account.
2. Click **Add New** -> **Project**.
3. Select your repository.
4. In the configuration settings:
   * **Framework Preset:** `Vite`
   * **Root Directory:** `frontend`
5. Expand **Environment Variables** and add:
   * **Key:** `VITE_API_BASE_URL`
   * **Value:** `https://your-backend-url.onrender.com/api` (use the Render URL from Step 2)
6. Click **Deploy**. Vercel will compile and deploy your React dashboard. Anyone can now access your app through the public Vercel URL!

---

## Option 2: Docker Compose Deployment (Self-Hosting on VPS)

If you have a Linux Virtual Private Server (VPS) (e.g., AWS EC2, DigitalOcean Droplet, Linode), you can deploy the entire stack with a single command using Docker.

### Prerequisites
Make sure your server has Docker and Docker Compose installed:
```bash
sudo apt update
sudo apt install docker.io docker-compose -y
```

### Steps to Deploy
1. Clone the repository on your server:
   ```bash
   git clone https://github.com/KavinC18/Smart-EV-charging-infrastructure-optimization.git
   cd Smart-EV-charging-infrastructure-optimization
   ```
2. Build and launch the containers in background mode:
   ```bash
   docker-compose up --build -d
   ```
3. That's it!
   * The **MySQL database** runs internally on port `3306`.
   * The **Spring Boot API** runs internally on port `8080`.
   * The **React Frontend** serves Nginx statically on port `80`.
   * Open your server's public IP address in your browser to view the application!

---

## Option 3: Deploying Frontend to GitHub Pages

If you wish to keep using GitHub Pages for the frontend, follow these steps:
1. In your GitHub repository settings, go to **Pages**.
2. Select **GitHub Actions** as the source instead of Deploy from a branch.
3. Commit a `.github/workflows/deploy.yml` deployment action that builds `/frontend` and deploys it.
4. Make sure to supply the `VITE_API_BASE_URL` build variable in the action to point to your hosted backend (e.g., on Render).
