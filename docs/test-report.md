# EvidenceVault Test Plan

## Integrity
- SHA-256 known-vector test
- Deterministic SHA-256 test
- InputStream/file hashing consistency
- Original evidence verification returns `VERIFIED`
- Modified evidence returns `TAMPERED`
- Missing evidence returns `VERIFICATION_FAILED`

## Authorization
- Valid login accepted
- Invalid login rejected
- ADMIN can access user management
- INVESTIGATOR cannot access `/users/**`
- Only ADMIN can archive/restore
- Investigator can access only owned cases
- Unauthorized case/evidence access produces `403`
- Unauthorized evidence download is rejected
- Unauthorized verification/report/history access is rejected

## Upload security
- Empty file rejected
- Oversized file rejected
- Disallowed extension rejected
- Path traversal filename is reduced to a safe basename
- Physical storage uses UUID filename
- Existing physical object cannot be overwritten by upload
- Archived case rejects new evidence

## Audit integrity
- Important security actions generate audit events
- Audit events contain previous and current SHA-256 fingerprints
- Audit chain verifies successfully after normal operation
- Manual modification of an audit row causes chain verification failure
- Older audit rows are backfilled when the audit-chain columns are first introduced

## Recommended manual penetration checks
1. Log in as investigator A.
2. Create a case and upload evidence.
3. Change the evidence ID in the browser URL to another investigator's evidence.
4. Confirm `403` and an `UNAUTHORIZED_ACCESS` event.
5. Change a stored evidence file and run verification.
6. Confirm `TAMPERED` plus `INTEGRITY_FAILURE`.
7. Attempt `POST /cases/{caseId}/archive` as investigator.
8. Confirm `403`.
9. Log in as admin and repeat; confirm archive succeeds.
10. Modify an audit row directly in MySQL and use the Audit page to confirm the chain reports a broken record.

## Automated security regression coverage added
- `CaseServiceAuthorizationTest`: investigator-to-investigator isolation and administrator access.
- `FileTypeValidatorTest`: content/type mismatch detection.
- `LoginSecurityServiceTest`: account lockout and failure-counter reset.
- `AuditServiceChainTest`: valid-chain acceptance and broken-link detection.

Run all tests with `mvn clean test`.
