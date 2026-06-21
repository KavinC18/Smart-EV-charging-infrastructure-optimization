# ChargeFlow Project Report
**AI-Powered EV Charging Grid Balancer & Infrastructure Optimization**

---

## 1. Executive Summary
**ChargeFlow** is a smart electric vehicle (EV) charging grid optimization platform designed to eliminate congestion at charging nodes. Rather than pointing drivers to the nearest charging station, ChargeFlow models charging queues using an **Erlang-C queueing model**, combined with predictive analytics to route vehicles to stations that minimize the *total expected charging delay* (travel time + expected waiting time + charging time) at the moment of arrival.

During this iteration, the infrastructure setup was fully centered on the metropolitan region of **Coimbatore, Tamil Nadu, India**, creating a localized seed database of grid nodes. Crucial UI bugs (layout overlap, click event validation handlers) and backend database lazy-load serialization errors were resolved, delivering a clean end-to-end user flow.

---

## 2. System Architecture
The application is split into a decoupled, responsive stack designed to support high-frequency telemetry simulation:
* **Backend Service (Spring Boot + MySQL):** Exposes a secure REST API handling user sessions, charging station configurations, active charging states, dynamic pricing surges, and logs.
* **Erlang-C Queue Prediction Engine:** Predicts wait times dynamically at all stations based on arrival/service rates.
* **Frontend Console (React + Vite + TypeScript):** Styled with custom dark-themed glassmorphism, featuring real-time maps (Leaflet) and chart analytics (Chart.js) tracking active stations.

---

## 3. Technical Achievements & Implementations

### 3.1 Geolocation Mapping Shift to Coimbatore
All five charging stations were centered within Coimbatore boundaries. Distance calculations utilize the Haversine formula to compute travel durations in real-time. The seeded stations are:
1. **Gandhipuram Grid Hub**
2. **Peelamedu Tech Node**
3. **Saravanampatti IT Park Charger**
4. **RS Puram Fast Charge Plaza**
5. **Singanallur Transport Hub**

### 3.2 Geolocation Live GPS Sync
The React driver console has been integrated with browser HTML5 Geolocation (`navigator.geolocation`) to retrieve the driver's real, live location on component mount. The app calculates distance metrics using these live coordinates, with manual override sliders available on the dashboard for manual coordinate adjustment.

### 3.3 UI Badge Overlap Resolution
Resolved a bug where the `★ Recommended Optimal` badge (previously absolutely positioned) overlapped the total delay text container. The badge was refactored in [DriverDashboard.tsx](file:///c:/Users/kavin/OneDrive/문서/Project/Smart%20EV%20charging%20infrastructure%20optimization/frontend/src/pages/DriverDashboard.tsx) to flow inline next to the station name header with responsive wrapping, maintaining look and feel on all screen widths.

### 3.4 Interactive Error Feedback & Scroll Action
Fixed a bug where clicking the "Start Charging Session" button failed silently without feedback when no vehicle was configured or active. The dashboard now:
* Smoothly scrolls the container to the top to focus the red error message banner.
* Displays a browser `alert` box so the warning is impossible to miss.

### 3.5 JPA Serialization Proxy Fix
Resolved a Jackson JSON serialization error caused by Hibernate's Lazy Loading mechanism. When fetching vehicle lists from the REST endpoints, the `Vehicle.user` lazy relation threw a `ByteBuddyInterceptor` type definition exception. Adding the `@JsonIgnore` decorator to the user relationship in [Vehicle.java](file:///c:/Users/kavin/OneDrive/문서/Project/Smart%20EV%20charging%20infrastructure%20optimization/backend/src/main/java/com/chargeflow/entity/Vehicle.java) resolved the serialization blocker.

---

## 4. Coimbatore Charging Network Seed Data

The following station records are seeded in the database:

| Station Name | Latitude | Longitude | Total Chargers | Base Price/kWh |
| :--- | :--- | :--- | :--- | :--- |
| **Gandhipuram Grid Hub** | 11.0173 | 76.9691 | 8 | $0.32 |
| **Peelamedu Tech Node** | 11.0264 | 76.9961 | 12 | $0.45 |
| **Saravanampatti IT Park Charger** | 11.0792 | 76.9997 | 6 | $0.28 |
| **RS Puram Fast Charge Plaza** | 11.0116 | 76.9452 | 10 | $0.38 |
| **Singanallur Transport Hub** | 11.0028 | 77.0225 | 4 | $0.35 |

---

## 5. Seed Users & Sandbox Credentials

Use these profiles to sign in, control the simulation, or test charging sessions:

| Username | Password | Role / Console | Seeded Vehicle |
| :--- | :--- | :--- | :--- |
| `admin` | `admin123` | `ROLE_ADMIN` (Grid Administrator) | None |
| `operator` | `operator123` | `ROLE_OPERATOR` (Station Operator) | None |
| `driver_kavin` | `driver123` | `ROLE_DRIVER` (EV Driver) | Hyundai Ioniq 5 |
| `tesla_sophia` | `driver123` | `ROLE_DRIVER` (EV Driver) | Tesla Model Y |
| `volt_john` | `driver123` | `ROLE_DRIVER` (EV Driver) | Hyundai Ioniq 5 |

---

## 7. Cloud Deployment & Production Architecture

To allow accessing the application on any device, the project was migrated from a local-only setup to a scalable, production-ready cloud deployment:

### 7.1 Component Hosting & Public Domains
- **React Frontend (Vercel):** Deployed to [smart-ev-charging-infrastructure-op.vercel.app](https://smart-ev-charging-infrastructure-op.vercel.app).
- **Spring Boot REST API (Render):** Hosted as a Dockerized web service at [smart-ev-charging-infrastructure.onrender.com](https://smart-ev-charging-infrastructure.onrender.com).
- **Relational Databases (Multi-Database Support):** Re-architected the backend configuration to support both **MySQL** (Railway/Aiven) and **PostgreSQL** (Neon/Aiven/Railway) dynamically.

### 7.2 Core Changes for Production Readiness
1. **Dynamic Environment Variables:** The Spring Boot backend dynamically configures its listening port (`${PORT}`), database credentials (`${SPRING_DATASOURCE_URL}`, `${SPRING_DATASOURCE_USERNAME}`, `${SPRING_DATASOURCE_PASSWORD}`), and CORS allowed origins (`${ALLOWED_ORIGINS}`) at startup.
2. **Database-Agnostic Setup:** Added the `org.postgresql:postgresql` dependency to `pom.xml` and removed hardcoded MySQL dialects from `application.properties`, allowing Hibernate to auto-detect the database type and configure its dialect dynamically.
3. **CORS Security Configuration:** Configured Spring Security to load CORS configurations from variables, allowing secure communication between the Vercel domain and the Render API.
4. **Vercel Build Alignment:** Resolved asset loading issues by configuring Vite's base path dynamically in `vite.config.ts` depending on the platform environment (using `/` for Vercel, and falling back to repository subdirectory for GitHub Pages).
5. **Local Multi-Device Preview:** Added `--host` mode configuration in `package.json` to allow developers to preview the application locally on secondary devices (such as smartphones) over the local Wi-Fi network.

---

## 8. Project Conclusion
With the completion of the Coimbatore grid seed, validation updates, and production cloud architecture, **ChargeFlow** is fully functional and ready for active driver usage on any device. The Erlang-C prediction engine computes expected queue times dynamically, dynamic pricing surges respond to usage levels, and the driver console handles geolocation and dashboard triggers cleanly. The application runs securely in the cloud and supports flexible database choices like Neon PostgreSQL and Railway MySQL out-of-the-box.
