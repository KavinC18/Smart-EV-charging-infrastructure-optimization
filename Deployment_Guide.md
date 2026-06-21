# ChargeFlow Public Cloud Deployment Guide

This guide outlines the steps to deploy the **ChargeFlow** full-stack application (Spring Boot Backend + React Vite Frontend + Database) to the public cloud so that it is accessible from any device.

---

## Architecture Overview

```
                                     +-----------------------------------+
                                     |        React Vite Frontend        |
                                     |    (Vercel or GitHub Pages / UI)  |
                                     +-----------------+-----------------+
                                                       |
                                                       | HTTP API Requests
                                                       v
                                     +-----------------+-----------------+
                                     |      Spring Boot REST API         |
                                     |       (Render or Railway)         |
                                     +-----------------+-----------------+
                                                       |
                                                       | JDBC Connection
                                                       v
                                     +-----------------+-----------------+
                                     |  MySQL (Railway/Aiven) OR         |
                                     |  PostgreSQL (Neon/Aiven/Railway)  |
                                     +-----------------------------------+
```

---

## Step 1: Deploy & Configure the Database

You can choose either **MySQL** or **PostgreSQL**. The Spring Boot backend dynamically auto-detects and configures itself based on the database URL provided.

### Option A: Neon (PostgreSQL) - Recommended for Postgres
1. Register/Login at [Neon.tech](https://neon.tech/).
2. Create a new project named `chargeflow`.
3. In the Neon dashboard, copy your **Connection String**. It will look like this:
   `postgresql://neondb_owner:pass123@ep-cool-water-a5.us-east-2.aws.neon.tech/neondb?sslmode=require`
4. Convert this URL to JDBC format by prefixing it with `jdbc:` and using `postgresql` (not `postgres`):
   * **JDBC URL:** `jdbc:postgresql://ep-cool-water-a5.us-east-2.aws.neon.tech/neondb?sslmode=require`
   * **Username:** `neondb_owner`
   * **Password:** `pass123`

### Option B: Railway MySQL
1. Sign up/Login at [Railway.app](https://railway.app/).
2. Click **New Project** -> **Provision MySQL**.
3. Under the MySQL service card, open the **Variables** tab and copy:
   * **JDBC URL:** `jdbc:mysql://${MYSQLHOST}:${MYSQLPORT}/${MYSQLDATABASE}?useSSL=false&allowPublicKeyRetrieval=true`
   * **Username:** `${MYSQLUSER}`
   * **Password:** `${MYSQLPASSWORD}`

### Option C: Aiven (MySQL or PostgreSQL)
1. Sign up/Login at [Aiven.io](https://aiven.io/).
2. Create a new service (select **MySQL** or **PostgreSQL**).
3. Find your Connection URI, Host, Port, User, and Password under the service overview.
4. Construct your JDBC connection string:
   * **MySQL:** `jdbc:mysql://<HOST>:<PORT>/<DATABASE_NAME>?useSSL=true`
   * **PostgreSQL:** `jdbc:postgresql://<HOST>:<PORT>/<DATABASE_NAME>?sslmode=require`

---

## Step 2: Deploy the Spring Boot Backend

You can host the Java backend on either **Render** or **Railway**.

### Option A: Render (Web Service)
1. Sign up/Login at [Render.com](https://render.com/).
2. Click **New** -> **Web Service**.
3. Connect your GitHub repository.
4. Set the following details:
   * **Name:** `chargeflow-backend`
   * **Root Directory:** `backend`
   * **Runtime:** `Docker` (Render automatically detects and builds the Dockerfile inside `/backend`)
5. Click **Advanced** and add the following **Environment Variables**:
   * `SPRING_DATASOURCE_URL` = Your JDBC URL (from Step 1)
   * `SPRING_DATASOURCE_USERNAME` = Your database username
   * `SPRING_DATASOURCE_PASSWORD` = Your database password
   * `JWT_SECRET` = A strong custom 64-character hex string (e.g. `9a62aa5f3c64e8e19e0b82f1bc2d89a6bc8b3d8753a8123abc456def78901234`)
   * `ALLOWED_ORIGINS` = `https://<your-vercel-domain>.vercel.app,https://<your-github-username>.github.io` (Separate multiple allowed frontend URLs with commas)
6. Click **Deploy Web Service**. Once deployed, copy your service's URL (e.g., `https://chargeflow-backend.onrender.com`).

### Option B: Railway (Docker)
1. On your Railway dashboard, click **New** -> **GitHub Repo**.
2. Connect your repository.
3. Railway will ask you to select the service to deploy. Select `backend`.
4. Go to **Settings** and set the Root Directory to `backend` (if not auto-detected).
5. Go to **Variables** and add the environment variables listed in the Render section above.
6. Under **Settings** -> **Service Domain**, generate a public domain to get your backend URL.

---

## Step 3: Deploy the React/Vite Frontend

### Option A: Vercel (Easiest & Recommended)
1. Sign up/Login at [Vercel.com](https://vercel.com/) and connect your GitHub account.
2. Click **Add New** -> **Project**.
3. Select your `Smart-EV-charging-infrastructure-optimization` repository.
4. In the Project Configuration:
   * **Framework Preset:** `Vite`
   * **Root Directory:** `frontend`
5. Expand the **Environment Variables** section and add:
   * **Key:** `VITE_API_BASE_URL`
   * **Value:** `https://your-backend-url.onrender.com/api` (Make sure to include `/api` suffix)
6. Click **Deploy**. Vercel will build and host your frontend.

### Option B: GitHub Pages
1. In your GitHub repository, navigate to **Settings** -> **Pages**.
2. Set the Source to **GitHub Actions**.
3. In your project local codebase, make sure a GitHub Actions file exists at `.github/workflows/deploy.yml` with instructions to build the `/frontend` directory and deploy to `gh-pages`.
4. Include the environment variable in the workflow:
   ```yaml
   env:
     VITE_API_BASE_URL: https://your-backend-url.onrender.com/api
   ```
5. Commit and push the workflow to run the build.

---

## Troubleshooting & Verification

### 1. Database Connection Failure
* Ensure that the JDBC URL begins with `jdbc:postgresql://` for PostgreSQL and `jdbc:mysql://` for MySQL.
* Verify you didn't include `<` or `>` brackets in your variables.
* Check database firewall settings (on Aiven or Neon, ensure external connections are allowed).

### 2. CORS Errors (Console Blocks Requests)
* If your frontend console displays CORS errors, verify that `ALLOWED_ORIGINS` in your backend environment variables exactly matches the URL of your deployed frontend (without trailing slashes), e.g. `https://chargeflow.vercel.app`.

### 3. JWT Signing issues
* Ensure your `JWT_SECRET` is set to a long, secure random key (at least 256-bit / 64 hex characters) to satisfy security requirements in production.
