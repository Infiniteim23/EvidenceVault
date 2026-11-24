CREATE TABLE IF NOT EXISTS roles (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(30) NOT NULL UNIQUE
);
CREATE TABLE IF NOT EXISTS users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(100) NOT NULL UNIQUE,
  email VARCHAR(150) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  role_id BIGINT NOT NULL,
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NULL,
  CONSTRAINT fk_users_role FOREIGN KEY(role_id) REFERENCES roles(id)
);
CREATE TABLE IF NOT EXISTS cases (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  case_id VARCHAR(30) NOT NULL UNIQUE,
  title VARCHAR(200) NOT NULL,
  description TEXT,
  investigation_type VARCHAR(50) NOT NULL,
  status VARCHAR(30) NOT NULL,
  created_by BIGINT NOT NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NULL,
  CONSTRAINT fk_cases_user FOREIGN KEY(created_by) REFERENCES users(id),
  INDEX idx_case_status(status)
);
CREATE TABLE IF NOT EXISTS evidence (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  case_id BIGINT NOT NULL,
  original_filename VARCHAR(255) NOT NULL,
  storage_filename VARCHAR(255) NOT NULL UNIQUE,
  mime_type VARCHAR(255) NOT NULL,
  file_size BIGINT NOT NULL,
  sha256_hash CHAR(64) NOT NULL,
  uploaded_by BIGINT NOT NULL,
  description TEXT,
  status VARCHAR(30) NOT NULL,
  uploaded_at DATETIME NOT NULL,
  CONSTRAINT fk_evidence_case FOREIGN KEY(case_id) REFERENCES cases(id),
  CONSTRAINT fk_evidence_user FOREIGN KEY(uploaded_by) REFERENCES users(id),
  INDEX idx_evidence_hash(sha256_hash)
);
CREATE TABLE IF NOT EXISTS evidence_verifications (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  evidence_id BIGINT NOT NULL,
  verified_by BIGINT NOT NULL,
  expected_hash CHAR(64) NOT NULL,
  calculated_hash CHAR(64) NOT NULL,
  result VARCHAR(30) NOT NULL,
  verified_at DATETIME NOT NULL,
  CONSTRAINT fk_verification_evidence FOREIGN KEY(evidence_id) REFERENCES evidence(id),
  CONSTRAINT fk_verification_user FOREIGN KEY(verified_by) REFERENCES users(id)
);
CREATE TABLE IF NOT EXISTS audit_logs (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NULL,
  actor_username VARCHAR(100),
  action VARCHAR(50) NOT NULL,
  entity_type VARCHAR(50),
  entity_id VARCHAR(100),
  timestamp DATETIME NOT NULL,
  result VARCHAR(30) NOT NULL,
  ip_address VARCHAR(45),
  details TEXT,
  previous_hash CHAR(64),
  integrity_hash CHAR(64),
  CONSTRAINT fk_audit_user FOREIGN KEY(user_id) REFERENCES users(id),
  INDEX idx_audit_time(timestamp),
  INDEX idx_audit_integrity(integrity_hash)
);
