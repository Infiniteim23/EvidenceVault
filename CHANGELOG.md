# EvidenceVault Change Log

## Security & Forensics Upgrade

### Access control
- Investigators are restricted to cases they created.
- Evidence view, verification, history, download and report operations enforce case ownership.
- Unauthorized access attempts are written to the audit stream.
- ADMIN-only archive and restore remains enforced by Spring Security and service-level checks.

### Evidence protection
- Physical evidence files use UUID-based storage names.
- Uploads use `CREATE_NEW` to avoid accidental overwrite.
- Actual stored file size is measured server-side before metadata is persisted.
- Archived cases reject new evidence.

### Integrity
- Evidence SHA-256 verification remains the core file-integrity control.
- Verification history records expected and calculated fingerprints.
- Evidence pages now show an access / chain-of-custody timeline.

### Audit integrity
- Audit records now contain `actor_username`, `previous_hash` and `integrity_hash`.
- A SHA-256 hash chain links audit records chronologically.
- Existing audit rows are backfilled at application startup when the new fields are missing.
- The Dashboard and Audit page report audit-chain health.
- Login success, login failure and logout are now audited.

### Web security
- CSRF remains enabled.
- Content Security Policy, frame denial and referrer-policy headers were added.
- The project documentation now includes authorization and audit-chain testing guidance.

### UI
- Cases, evidence, audit and verification pages use the same EvidenceVault visual language.
- Text-first navigation actions replace generic boxed buttons.
- Motion design, forensic fingerprint panels, evidence timelines and responsive layouts were added.
- Reduced-motion support is retained.

### Role-scoped audit visibility
- Full security audit is now ADMIN-only at the HTTP authorization layer and method-security layer.
- INVESTIGATOR users no longer see the Audit navigation item on dashboard, case list, or case details pages.
- INVESTIGATOR dashboard activity is limited to the signed-in user's own recent activity.
- Audit-chain verification status is no longer exposed on the investigator dashboard.
- Case/evidence-specific chain-of-custody history remains available through the authorized evidence workflows.


## Role-scoped dashboard metrics
- ADMIN dashboard metrics remain system-wide.
- INVESTIGATOR dashboard metrics are restricted to cases created by the signed-in investigator.
- INVESTIGATOR no longer receives global evidence/tampering counts.
- Dashboard Audit navigation uses a direct Spring Security role expression in the template and remains ADMIN-only.

## Production Hardening Upgrade
- Added Flyway migrations and changed JPA schema management to `validate`.
- Removed the database password fallback from the default configuration.
- Added login rate limiting, configurable account lockout and session-fixation protection.
- Added MIME/content inspection with Apache Tika.
- Added optional ClamAV malware scanning that fails closed when enabled.
- Added atomic `CREATE_NEW` evidence storage and an `EvidenceStorage` abstraction.
- Added production profile, secure session cookies, HSTS and Docker/Nginx deployment examples.
- Added MySQL/evidence backup scripts and recovery documentation.
- Added request IDs and safer generic error handling.
- Added automated regression tests for authorization, upload content validation, login lockout and audit-chain integrity.

## 2026-11-24 — Build Fix
- Fixed `CustomUserDetailsService` name collision between the application `User` entity and Spring Security's `User` builder.
- Fixed invalid use of `StandardCopyOption.CREATE_NEW`; evidence storage now uses `StandardOpenOption.CREATE_NEW` with bounded streaming writes.
- Oversized uploads are removed completely and filename collisions fail safely.
- Added automated tests for atomic evidence creation, collision handling, and oversized-file cleanup.
