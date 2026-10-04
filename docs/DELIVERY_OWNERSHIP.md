# AI Asset Marketplace Delivery Ownership

## Goal
Deliver the marketplace successfully from development through QA and free production deployment.

## Leo — Senior Developer
Owns product implementation across the separated architecture:
- `frontend/` React + Vite
- `backend/` Java 21 business/service layer
- `api/` Spring Boot REST API

Leo handles features, bug fixes, API integration, and code changes requested by QA or deployment.

## Lina — Senior Q/A
Owns validation and release quality:
- smoke testing
- desktop/tablet/mobile testing
- authentication and role testing
- seller/buyer/admin workflow testing
- API validation and error handling
- regression testing after Leo fixes issues
- final production smoke test

Lina reports defects back to Leo until release criteria are met.

## Noah — Senior Cloud Operator
Owns deployment and CI/CD:
- evaluate free hosting options for frontend, Java API, and database
- select a zero-cost deployment architecture where practical
- configure environment variables and CORS
- create/maintain CI build and deployment workflows
- validate Java API health checks
- coordinate production API URL with Leo
- coordinate production smoke tests with Lina
- investigate deployment/runtime failures
- document deployment and rollback steps

Noah should be involved before deployment changes are merged and during every production deployment.

## Alex — Manager / Blocker Owner
When any delivery agent raises a blocker:
1. Alex receives and reviews the blocker first.
2. Alex should resolve coordination, priority, dependency, access, or decision blockers directly where possible.
3. If technical help is needed, Alex routes the blocker to the correct specialist (Leo, Lina, Noah, Mina, Sam, or Kai).
4. If Alex cannot resolve the blocker, it is escalated to the project owner for further review instead of leaving the task stalled.

## Release gate
The project is considered successful only when:
- Leo confirms implementation is complete.
- Lina confirms QA/regression and production smoke testing pass.
- Noah confirms frontend, API/backend, database, environment configuration, and CI/CD are operational.
- No unresolved blocker remains.
- Production URLs and deployment instructions are documented.
