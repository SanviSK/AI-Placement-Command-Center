# AI Placement Command Center

A full-stack platform that helps engineering students track and improve their campus placement readiness — combining resume intelligence, DSA practice, project recommendations, application tracking, and AI-powered mock interviews into a single dashboard.

## Overview

Students upload their resume, academic marks, skills, and target companies. The platform then:

- Calculates a weighted **Placement Readiness Score** across academics, DSA progress, projects, skill match, and resume completeness
- Parses resumes using AI and extracts structured profile data (education, experience, projects, certifications)
- Generates a **daily DSA task list** targeting weak topics, with streak tracking and a solve-history heatmap
- Recommends **projects** that fill skill gaps against target company requirements
- Tracks job applications on a **Kanban board** (Applied → OA → Interview → Offer → Rejected) with stage history
- Generates **ATS-tailored resumes** per job description using AI, with PDF/DOCX export
- Runs **AI mock interviews** (technical / HR / behavioral) with per-answer scoring and feedback

## Tech Stack

**Frontend**
- React.js (Vite) + Tailwind CSS
- Recharts for data visualization
- Axios for API communication

**Backend**
- Java 17 + Spring Boot 3
- Spring Security + JWT authentication
- Spring Data JPA / Hibernate
- PostgreSQL

**AI Layer**
- Google Gemini API — resume parsing, ATS resume generation, mock interview question generation & scoring

**Other**
- Apache PDFBox / POI — resume file parsing and document export

## Features Implemented

| Module | Status |
|---|---|
| Auth (JWT) & Student Profile | ✅ |
| Resume Upload & AI Parsing | ✅ |
| Readiness Score Engine | ✅ |
| DSA Daily Task Engine | ✅ |
| Project Recommender | ✅ |
| Application Tracker (Kanban) | ✅ |
| ATS Resume Generator | ✅ |
| AI Mock Interview | ✅ |
| Weekly Study Plan Generator | 🚧 In Progress |
| Job Monitoring | 📋 Planned |
| Cloud Deployment | 📋 Planned |

## Architecture

```
├── backend/          Spring Boot REST API
│   └── src/main/java/com/placement/commandcenter/
│       ├── controller/    REST endpoints
│       ├── service/       Business logic
│       ├── repository/    JPA repositories
│       ├── entity/        Database models
│       ├── dto/           Request/response objects
│       └── security/      JWT auth, filters
└── frontend/          React (Vite) SPA
    └── src/
        ├── pages/         Dashboard, Auth pages
        ├── context/       Auth context
        └── utils/         API client
```

## Running Locally

**Prerequisites:** Java 17, Maven, Node.js, PostgreSQL

```bash
# Backend
cd backend
mvn spring-boot:run

# Frontend (separate terminal)
cd frontend
npm install
npm run dev
```

Set your own `GEMINI_API_KEY` and database credentials as environment variables — see `application.yml` for expected variable names.

## Author

Built by [Sanvi S K](https://github.com/SanviSK) — Computer Science and Engineering, Dayananda Sagar Academy of Technology and Management.
