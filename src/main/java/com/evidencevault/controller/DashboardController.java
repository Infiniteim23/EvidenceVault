package com.evidencevault.controller;

import com.evidencevault.repository.CaseRepository;
import com.evidencevault.repository.EvidenceRepository;
import com.evidencevault.service.AuditService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final CaseRepository cases;
    private final EvidenceRepository evidence;
    private final AuditService audit;

    public DashboardController(
            CaseRepository cases,
            EvidenceRepository evidence,
            AuditService audit) {
        this.cases = cases;
        this.evidence = evidence;
        this.audit = audit;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        boolean admin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);

        // ADMIN sees global system metrics. INVESTIGATOR sees only metrics
        // derived from cases created by the signed-in investigator.
        if (admin) {
            model.addAttribute("cases", cases.count());
            model.addAttribute("evidence", evidence.count());
            model.addAttribute("verified", evidence.countByStatus("VERIFIED"));
            model.addAttribute("tampered", evidence.countByStatus("TAMPERED"));
            model.addAttribute("pending", evidence.countByStatus("PENDING"));
        } else {
            String username = authentication.getName();
            model.addAttribute("cases", cases.findByCreatedByUsernameOrderByCreatedAtDesc(username).size());
            model.addAttribute("evidence", evidence.countByInvestigationCaseCreatedByUsername(username));
            model.addAttribute("verified", evidence.countByInvestigationCaseCreatedByUsernameAndStatus(username, "VERIFIED"));
            model.addAttribute("tampered", evidence.countByInvestigationCaseCreatedByUsernameAndStatus(username, "TAMPERED"));
            model.addAttribute("pending", evidence.countByInvestigationCaseCreatedByUsernameAndStatus(username, "PENDING"));
        }
        model.addAttribute("isAdmin", admin);
        model.addAttribute("audits", admin ? audit.recent() : audit.recentForUser(authentication.getName()));
        if (admin) {
            model.addAttribute("auditChain", audit.verifyChain());
        }
        model.addAttribute("currentUser", authentication.getName());
        return "dashboard/index";
    }
}
