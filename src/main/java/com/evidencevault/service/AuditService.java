package com.evidencevault.service;

import com.evidencevault.entity.*;
import com.evidencevault.repository.*;
import com.evidencevault.util.HashUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditService {
    private static final String GENESIS = "GENESIS";

    private final AuditLogRepository logs;
    private final UserRepository users;

    public AuditService(AuditLogRepository logs,UserRepository users){this.logs=logs;this.users=users;}

    /**
     * Adds a tamper-evident audit event. The previous event hash is included in
     * the current event fingerprint, creating a simple hash chain.
     */
    @Transactional
    public synchronized void log(String username,String action,String type,String id,String result,String ip,String details){
        AuditLog a=new AuditLog();
        a.setActorUsername(username == null ? "unknown" : username);
        users.findByUsername(username).ifPresent(a::setUser);
        a.setAction(action); a.setEntityType(type); a.setEntityId(id); a.setResult(result);
        a.setIpAddress(ip); a.setDetails(details); a.setTimestamp(LocalDateTime.now());

        AuditLog previous = logs.findTopByOrderByIdDesc();
        String previousHash = previous == null || previous.getIntegrityHash() == null
                ? GENESIS : previous.getIntegrityHash();
        a.setPreviousHash(previousHash);
        logs.save(a);
        a.setIntegrityHash(calculateHash(a, previousHash));
        logs.save(a);
    }

    public List<AuditLog> recent(){return logs.findTop20ByOrderByTimestampDesc();}

    /** Returns only the signed-in investigator's own activity for the dashboard. */
    public List<AuditLog> recentForUser(String username){
        return logs.findTop20ByActorUsernameOrderByTimestampDesc(username);
    }

    public List<AuditLog> forEntity(String type, String id){return logs.findByEntityTypeAndEntityIdOrderByTimestampAsc(type,id);}

    @Transactional
    public AuditChainStatus verifyChain(){
        List<AuditLog> all=logs.findAllByOrderByIdAsc();
        String previous=GENESIS;
        for(AuditLog a: all){
            if(a.getPreviousHash()==null || !previous.equals(a.getPreviousHash())){
                return new AuditChainStatus(false,a.getId(),"Previous-hash link mismatch",all.size());
            }
            String expected=calculateHash(a,previous);
            if(a.getIntegrityHash()==null || !expected.equalsIgnoreCase(a.getIntegrityHash())){
                return new AuditChainStatus(false,a.getId(),"Audit record fingerprint mismatch",all.size());
            }
            previous=a.getIntegrityHash();
        }
        return new AuditChainStatus(true,null,"Audit chain verified",all.size());
    }

    /** Rebuilds the entire chain only when legacy rows are missing chain fields. */
    @Transactional
    public void backfillMissingChain(){
        List<AuditLog> all=logs.findAllByOrderByIdAsc();
        if(all.isEmpty()) return;
        boolean needsRebuild=all.stream().anyMatch(a -> a.getPreviousHash()==null || a.getIntegrityHash()==null);
        if(!needsRebuild) return;
        String previous=GENESIS;
        for(AuditLog a: all){
            if(a.getActorUsername()==null) a.setActorUsername(a.getUser()==null?"system":a.getUser().getUsername());
            a.setPreviousHash(previous);
            a.setIntegrityHash(calculateHash(a,previous));
            previous=a.getIntegrityHash();
        }
        logs.saveAll(all);
    }

    private String calculateHash(AuditLog a,String previousHash){
        String payload = String.join("|",
                safe(a.getId()), safe(a.getTimestamp()), safe(a.getActorUsername()),
                safe(a.getAction()), safe(a.getEntityType()), safe(a.getEntityId()), safe(a.getResult()),
                safe(a.getIpAddress()), safe(a.getDetails()), safe(previousHash));
        return HashUtil.sha256(payload);
    }

    private String safe(Object value){return value==null?"":String.valueOf(value);}

    public record AuditChainStatus(boolean valid, Long brokenRecordId, String message, int checkedRecords) {}
}
