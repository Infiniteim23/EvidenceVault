package com.evidencevault.controller;

import com.evidencevault.service.AuditService;
import org.springframework.stereotype.Controller;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuditController {
    private final AuditService audit;
    public AuditController(AuditService a){audit=a;}

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/audit")
    String audit(Model m){
        m.addAttribute("logs",audit.recent());
        m.addAttribute("chain",audit.verifyChain());
        return "audit/list";
    }
}
