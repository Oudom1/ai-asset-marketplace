# AI Asset Marketplace

Full-stack marketplace for browsing and selling digital AI assets.

## Stack
- Frontend: React + Vite
- Backend: Java 21 business/service layer
- API: Java 21 + Spring Boot REST service

## Structure
- `frontend/` React web application only
- `backend/` Java domain/business/service logic only
- `api/` Spring Boot REST API only
- `pom.xml` parent Maven build for backend + API

## Architecture

```text
React Frontend
     |
     | HTTP/JSON
     v
Spring Boot API
     |
     v
Java Backend Services
     |
     v
Database / persistence layer
```

The frontend must not contain backend business logic. The API owns HTTP controllers and request/response handling. The backend owns marketplace business rules, services, and persistence.

## Run locally

### Build Java modules
```bash
mvn clean install
```

### API
```bash
cd api
mvn spring-boot:run
```

API default address: `http://localhost:8080/api`

### Frontend
```bash
cd frontend
npm install
npm run dev
```

Configure the frontend production API base URL with an environment variable when the backend API is deployed.
