# EvidenceVault — Digital Evidence Integrity & Management System

EvidenceVault is a Java/Spring Boot + MySQL academic cybersecurity application for managing investigation cases, securely storing digital evidence, calculating SHA-256 integrity fingerprints, verifying evidence, detecting possible tampering, retaining verification history, maintaining an audit trail, and generating downloadable evidence reports.

## Technology Stack
- Java 17+
- Spring Boot 3.5.x
- Spring MVC / Thymeleaf
- Spring Data JPA / Hibernate
- Spring Security + BCrypt
- Flyway database migrations
- Apache Tika content validation
- Optional ClamAV malware scanning
- MySQL 8+
- Bootstrap 5 + custom CSS
- Maven
- JUnit 5

## Implemented Modules
1. Secure login with Spring Security
2. ADMIN and INVESTIGATOR roles
3. Investigation case registration and listing
4. Secure evidence upload with file-type and size validation
5. Randomized evidence storage filenames
6. Server-side SHA-256 generation
7. Evidence metadata and case association
8. Evidence download for authenticated users
9. Integrity verification with VERIFIED / TAMPERED / VERIFICATION_FAILED states
10. Verification history with expected and calculated hashes
11. Tamper-evident SHA-256 audit chain for case, evidence, verification and download events
12. Dashboard with current integrity statistics
13. Admin-only user listing
14. Downloadable plain-text evidence integrity report
15. CSRF protection on browser forms
16. Path traversal protection for physical evidence storage
17. Server-side ownership authorization to prevent cross-case evidence access / IDOR
18. CSP, frame-denial and referrer-policy security headers
19. Audit-chain verification dashboard
20. Verification history and evidence fingerprint report
21. Login rate limiting and account lockout
22. MIME/magic-byte content validation
23. Atomic evidence creation with `CREATE_NEW`
24. Evidence storage abstraction
25. Production profile with HSTS and secure cookies
26. Docker/Nginx/ClamAV deployment example
27. MySQL backup scripts and recovery guidance
28. Automated authorization, upload-security, login-security and audit-chain regression tests

## Security model
The application never uses the user-supplied original filename as the physical storage filename. Evidence is stored under a UUID-based `.bin` filename. SHA-256 is calculated by the server during upload and again during verification. The stored hash is compared with the newly calculated hash to detect content changes. Evidence access is also authorization-checked against the owning case: administrators can access all cases, while investigators can access only cases they created.

Audit records form a chronological SHA-256 hash chain using `previous_hash` and `integrity_hash`. The application verifies this chain on the Audit page to detect modified or deleted audit events.

SHA-256 proves file-content integrity. It does not, by itself, prove legal authenticity, identify the person who originally created a file, or constitute a complete legal chain of custody.

## Run
1. Install JDK 17+ and Maven.
2. Start MySQL.
3. Create the database:
   `CREATE DATABASE evidencevault;`
4. Configure `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, and optionally `STORAGE_PATH` if required.
5. Run:
   `mvn clean test`
   `mvn spring-boot:run`
6. Open `http://localhost:8080/login`.

## Demo accounts
- ADMIN: `admin` / `Admin@123`
- INVESTIGATOR: `investigator` / `Investigator@123`

For local development, demo accounts can be seeded. For production, set `APP_SEED_ENABLED=false`; the production profile disables demo-user creation and requires credentials through environment variables/secrets.

## Demonstration workflow
1. Login as investigator.
2. Create an investigation case.
3. Open the case and upload `server-log.txt` or another allowed evidence file.
4. Open the evidence record and note the generated SHA-256 fingerprint.
5. Click **Verify Integrity** — the expected result is `VERIFIED`.
6. In a controlled local demonstration, modify the corresponding stored evidence file.
7. Verify again — the expected result is `TAMPERED`.
8. Open **Verification History** to show the expected/calculated hashes and timestamps.
9. Open **Audit Trail** to show integrity verification and failure events.
10. Download the evidence integrity report.

## Project architecture
`Controller -> Service -> Repository -> MySQL`

Security, audit, exception handling, entities, repositories and utility components are separated into their own packages.

## Storage
Default evidence storage:
`./storage/evidence`

Maximum configured evidence size:
`20 MB`

Allowed extensions:
`pdf, txt, csv, jpg, jpeg, png, docx, zip, log`

## Production notes
For a higher-assurance forensic deployment, add immutable/WORM audit storage or external hash anchoring, cryptographic signatures/key management, object-storage versioning, retention policies, forensic acquisition integrations, access reviews, and centralized security monitoring. The included production profile already provides migrations, secure cookies, HSTS, rate limiting, account lockout, content validation, optional ClamAV scanning, Docker/Nginx deployment, and backup guidance.
