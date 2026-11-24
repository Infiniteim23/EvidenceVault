# Production Deployment

## Recommended topology
Nginx (TLS) -> Spring Boot -> MySQL
                         -> private evidence volume
                         -> optional ClamAV scanner

## Configuration
Use environment variables or a secrets manager. Do not commit database passwords or production seed passwords.

Production runs with `spring.profiles.active=prod`, JPA `ddl-auto=validate`, Flyway migrations, secure session cookies and HSTS.

## Evidence storage
Keep the evidence volume private and writable only by the application service account. Back it up separately from the database. For higher assurance, replace `LocalEvidenceStorage` with an object-storage implementation using versioning and immutable retention.

## TLS
The included Nginx configuration expects `certs/fullchain.pem` and `certs/privkey.pem`. Use a trusted certificate in real deployment. Never commit private keys.

## Malware scanning
The production Compose example enables ClamAV. If scanning is enabled, an unavailable scanner fails evidence ingestion rather than silently accepting unscanned evidence.

## Backups
Run `scripts/backup-mysql.sh` or `scripts/backup-mysql.ps1` regularly. Test restores periodically. Back up both the MySQL database and the evidence storage volume.

## Recovery
1. Restore MySQL backup.
2. Restore the matching evidence storage snapshot.
3. Start the application and allow Flyway validation/migrations.
4. Verify audit-chain integrity.
5. Verify representative evidence hashes.

## Production hardening checklist
- Rotate all demo/default credentials.
- Restrict database network access.
- Use a secrets manager where available.
- Use immutable/versioned evidence storage for forensic deployments.
- Send application/audit logs to a separate security monitoring system.
- Configure alerting for integrity failures and repeated login failures.

For filesystem evidence, use `scripts/backup-evidence.sh` in addition to the MySQL backup. Database and evidence-volume backups should be versioned together so evidence metadata and bytes can be restored consistently.
