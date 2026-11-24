package com.evidencevault.config;

import com.evidencevault.service.AuditService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class AuditChainInitializer implements ApplicationRunner {
    private final AuditService auditService;
    public AuditChainInitializer(AuditService auditService){this.auditService=auditService;}
    @Override public void run(ApplicationArguments args){auditService.backfillMissingChain();}
}
