package com.evidencevault.service;

import com.evidencevault.entity.*;
import com.evidencevault.repository.*;
import com.evidencevault.util.CaseIdGenerator;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CaseService {
    private final CaseRepository repo;
    private final UserRepository users;
    private final CaseIdGenerator ids;
    private final AuditService audit;

    public CaseService(CaseRepository r, UserRepository u, CaseIdGenerator i, AuditService a) {
        repo=r; users=u; ids=i; audit=a;
    }

    public InvestigationCase create(String username,String title,String desc,String type,String ip){
        User u=users.findByUsername(username).orElseThrow();
        if(title==null || title.isBlank() || title.length()>200) throw new IllegalArgumentException("Case title is required and must be 200 characters or less");
        if(type==null || type.isBlank() || type.length()>50) throw new IllegalArgumentException("Investigation type is required");
        InvestigationCase c=new InvestigationCase();
        c.setCaseId(ids.next()); c.setTitle(title.trim()); c.setDescription(desc==null?"":desc.trim());
        c.setInvestigationType(type.trim()); c.setCreatedBy(u); repo.save(c);
        audit.log(username,"CASE_CREATED","CASE",c.getCaseId(),"SUCCESS",ip,"Case created");
        return c;
    }

    public List<InvestigationCase> all(){return repo.findAllByOrderByCreatedAtDesc();}

    public List<InvestigationCase> visibleTo(String username){
        User u=users.findByUsername(username).orElseThrow();
        return isAdmin(u) ? repo.findAllByOrderByCreatedAtDesc() : repo.findByCreatedByUsernameOrderByCreatedAtDesc(username);
    }

    public InvestigationCase get(String id){return repo.findByCaseId(id).orElseThrow();}

    public InvestigationCase getAuthorized(String id,String username,String ip){
        InvestigationCase c=get(id);
        ensureAccess(c,username,ip,"CASE_VIEWED");
        return c;
    }

    public boolean canAccess(InvestigationCase c,String username){
        User u=users.findByUsername(username).orElse(null);
        return u!=null && (isAdmin(u) || (c.getCreatedBy()!=null && username.equals(c.getCreatedBy().getUsername())));
    }

    public void ensureAccess(InvestigationCase c,String username,String ip,String action){
        if(!canAccess(c,username)){
            audit.log(username,"UNAUTHORIZED_ACCESS","CASE",c.getCaseId(),"FAILURE",ip,action+" denied");
            throw new org.springframework.security.access.AccessDeniedException("You are not authorized to access this case");
        }
        if("CASE_VIEWED".equals(action)) audit.log(username,action,"CASE",c.getCaseId(),"SUCCESS",ip,"Case viewed");
    }

    private boolean isAdmin(User u){return u.getRole()!=null && "ADMIN".equalsIgnoreCase(u.getRole().getName());}

    public long count(){return repo.count();}
    public long activeCount(){return repo.countByStatus("OPEN");}
    public long archivedCount(){return repo.countByStatus("ARCHIVED");}

    public InvestigationCase archive(String caseId,String username,String ip){
        InvestigationCase c=get(caseId); ensureAccess(c,username,ip,"CASE_ARCHIVE");
        if(!isAdmin(users.findByUsername(username).orElseThrow())) throw new org.springframework.security.access.AccessDeniedException("Only administrators can archive cases");
        if("ARCHIVED".equalsIgnoreCase(c.getStatus())) throw new IllegalStateException("Case is already archived");
        c.setStatus("ARCHIVED"); repo.save(c);
        audit.log(username,"CASE_ARCHIVED","CASE",c.getCaseId(),"SUCCESS",ip,"Case archived; evidence retained");
        return c;
    }

    public InvestigationCase restore(String caseId,String username,String ip){
        InvestigationCase c=get(caseId); ensureAccess(c,username,ip,"CASE_RESTORE");
        if(!isAdmin(users.findByUsername(username).orElseThrow())) throw new org.springframework.security.access.AccessDeniedException("Only administrators can restore cases");
        if(!"ARCHIVED".equalsIgnoreCase(c.getStatus())) throw new IllegalStateException("Case is not archived");
        c.setStatus("OPEN"); repo.save(c);
        audit.log(username,"CASE_RESTORED","CASE",c.getCaseId(),"SUCCESS",ip,"Case restored");
        return c;
    }
}
