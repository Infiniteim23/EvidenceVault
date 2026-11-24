# Security Notes

EvidenceVault is designed as a security-focused academic application. The security model is deliberately enforced in the backend; UI controls are only a convenience layer.

## Authentication and authorization
- Spring Security handles authentication and session management.
- Passwords are stored using BCrypt, never plaintext.
- ADMIN and INVESTIGATOR roles are enforced server-side.
- `/users/**` and case archive/restore operations require ADMIN.
- Investigators can access only cases they created; administrators can access all cases.
- Evidence view, verification, history, download and report operations re-check the owning case before serving the resource.
- Unauthorized access attempts are recorded as `UNAUTHORIZED_ACCESS` audit events.

## Evidence storage
- Original filenames are sanitized with `Path.getFileName()` and are never used as physical storage paths.
- Physical evidence names are UUID-based `.bin` files.
- Storage paths are normalized and must remain directly under the configured evidence root.
- Files are created with `CREATE_NEW` so an existing storage object is never silently overwritten.
- Uploads enforce a 20 MB limit and extension allowlisting.
- Server-side SHA-256 is calculated from the stored bytes.

## Integrity verification
- The upload-time SHA-256 becomes the registered evidence fingerprint.
- Verification recalculates SHA-256 from the current stored bytes.
- A match produces `VERIFIED`; a mismatch produces `TAMPERED`; a missing file produces `VERIFICATION_FAILED`.
- Every verification stores expected hash, calculated hash, user and timestamp.

## Tamper-evident audit chain
Each audit event contains:
- `previous_hash`
- `integrity_hash`

The current event fingerprint includes its data plus the previous event fingerprint. This creates a chronological SHA-256 hash chain. The Audit page verifies the chain and reports the first broken record.

This is **tamper-evident**, not a cryptographic guarantee against a database administrator who can rewrite every row and recompute the entire chain. Production systems should anchor audit hashes externally or use immutable/WORM storage.

## HTTP security
- CSRF protection is enabled for browser forms.
- Content Security Policy, frame denial and referrer-policy headers are configured.
- Secure error pages avoid exposing stack traces.
- Secrets should be supplied through environment variables rather than committed configuration.

## Production enhancements
Add MIME/content inspection, antivirus or sandbox scanning, rate limiting, MFA, immutable/WORM audit storage, external hash anchoring, centralized monitoring, key management, retention policies, and stronger forensic acquisition controls before using the system for real investigations.

## Audit visibility and least privilege
The global security audit is restricted to administrators. Investigators do not receive the `/audit` route and the UI hides the global Audit navigation. The investigator dashboard only shows the signed-in investigator's own recent activity. Evidence-specific chain-of-custody history remains available only when the investigator is authorized to access that evidence.

## File ingestion hardening
- Evidence is stored with `CREATE_NEW` to prevent silent physical overwrites.
- Apache Tika validates the detected file content against the allowlisted extension before registration.
- Optional ClamAV scanning is integrated; when enabled, scanner unavailability rejects ingestion rather than bypassing the control.
- A storage abstraction (`EvidenceStorage`) keeps filesystem-specific behavior out of the evidence domain and allows a future private object-storage implementation.

## Authentication hardening
- Login requests are rate-limited per source IP.
- Repeated failures lock an account for a configurable period.
- Successful authentication clears failure counters.
- Sessions use fixation protection and a bounded concurrent-session count.
- Production profile enables secure session cookies and HSTS.

## Database and secrets
- Flyway manages schema migrations and JPA runs with `ddl-auto=validate`.
- Production seed-user creation is disabled.
- Database credentials are supplied through environment variables/secrets rather than source-controlled defaults.
