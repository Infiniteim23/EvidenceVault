package com.evidencevault.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="audit_logs", indexes={
        @Index(name="idx_audit_time", columnList="timestamp"),
        @Index(name="idx_audit_integrity", columnList="integrity_hash")
})
public class AuditLog {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="user_id")
    private User user;

    /** Immutable username snapshot used by the audit-chain fingerprint. */
    @Column(name="actor_username", length=100)
    private String actorUsername;

    @Column(nullable=false,length=50) private String action;
    @Column(length=50) private String entityType;
    @Column(length=100) private String entityId;
    @Column(name="timestamp",nullable=false) private LocalDateTime timestamp;
    @Column(nullable=false,length=30) private String result;
    @Column(length=45) private String ipAddress;
    @Column(columnDefinition="TEXT") private String details;

    /** Hash of the previous audit event in the tamper-evident chain. */
    @Column(name="previous_hash",length=64)
    private String previousHash;

    /** SHA-256 fingerprint of this audit event plus previousHash. */
    @Column(name="integrity_hash",length=64)
    private String integrityHash;

    @PrePersist void pre(){if(timestamp==null)timestamp=LocalDateTime.now();}

    public Long getId(){return id;}
    public User getUser(){return user;}
    public void setUser(User v){user=v;}
    public String getActorUsername(){return actorUsername;}
    public void setActorUsername(String v){actorUsername=v;}
    public String getAction(){return action;}
    public void setAction(String v){action=v;}
    public String getEntityType(){return entityType;}
    public void setEntityType(String v){entityType=v;}
    public String getEntityId(){return entityId;}
    public void setEntityId(String v){entityId=v;}
    public LocalDateTime getTimestamp(){return timestamp;}
    public void setTimestamp(LocalDateTime v){timestamp=v;}
    public String getResult(){return result;}
    public void setResult(String v){result=v;}
    public String getIpAddress(){return ipAddress;}
    public void setIpAddress(String v){ipAddress=v;}
    public String getDetails(){return details;}
    public void setDetails(String v){details=v;}
    public String getPreviousHash(){return previousHash;}
    public void setPreviousHash(String v){previousHash=v;}
    public String getIntegrityHash(){return integrityHash;}
    public void setIntegrityHash(String v){integrityHash=v;}
}
