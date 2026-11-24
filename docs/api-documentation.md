# API / HTTP Surface

The current academic UI exposes the following authenticated application routes:

- GET `/dashboard`
- GET `/cases`
- GET `/cases/new`
- POST `/cases`
- GET `/cases/{caseId}`
- GET `/evidence/upload/{caseId}`
- POST `/evidence/upload/{caseId}`
- GET `/evidence/{id}`
- POST `/evidence/{id}/verify`
- GET `/evidence/{id}/download`
- GET `/audit`
- GET `/users` (ADMIN)

The service layer is structured so JSON REST controllers can be added without moving business logic out of services.
