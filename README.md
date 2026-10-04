# AI Asset Marketplace

Full-stack marketplace for browsing and selling digital AI assets.

## Stack
- Frontend: React + Vite
- Backend: Java 21 + Spring Boot
- API: REST

## Structure
- `frontend/` React application
- `backend/` Spring Boot API

## Run locally

### Backend
```bash
cd backend
mvn spring-boot:run
```

### Frontend
```bash
cd frontend
npm install
npm run dev
```

The frontend expects the API at `http://localhost:8080/api`.
