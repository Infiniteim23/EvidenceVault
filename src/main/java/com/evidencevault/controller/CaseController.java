package com.evidencevault.controller;

import com.evidencevault.service.CaseService;
import com.evidencevault.service.EvidenceService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/cases")
public class CaseController {
    private final CaseService service;
    private final EvidenceService evidenceService;
    private final com.evidencevault.service.AuditService auditService;

    public CaseController(CaseService service, EvidenceService evidenceService, com.evidencevault.service.AuditService auditService){this.service=service;this.evidenceService=evidenceService;this.auditService=auditService;}

    @GetMapping
    public String list(Model model, Authentication authentication){
        model.addAttribute("cases", service.visibleTo(authentication.getName()));
        return "cases/list";
    }

    @GetMapping("/new") public String form(){return "cases/form";}

    @PostMapping
    public String create(@RequestParam String title,@RequestParam String description,@RequestParam String investigationType,
                         Authentication authentication,HttpServletRequest request){
        service.create(authentication.getName(),title,description,investigationType,clientIp(request));
        return "redirect:/cases";
    }

    @GetMapping("/{caseId}")
    public String details(@PathVariable String caseId,Model model,Authentication authentication,HttpServletRequest request){
        var investigationCase=service.getAuthorized(caseId,authentication.getName(),clientIp(request));
        model.addAttribute("case",investigationCase);
        model.addAttribute("evidenceList",evidenceService.byCase(investigationCase.getId()));
        // The case has already passed resource-level authorization above.
        // Therefore this timeline contains only activity for this authorized case,
        // including administrator actions such as archive/restore/view.
        model.addAttribute("caseTimeline", auditService.forEntity("CASE", investigationCase.getCaseId()));
        return "cases/details";
    }

    @PostMapping("/{caseId}/archive") @PreAuthorize("hasRole('ADMIN')")
    public String archive(@PathVariable String caseId,Authentication authentication,HttpServletRequest request){
        service.archive(caseId,authentication.getName(),clientIp(request));
        return "redirect:/cases/"+caseId;
    }

    @PostMapping("/{caseId}/restore") @PreAuthorize("hasRole('ADMIN')")
    public String restore(@PathVariable String caseId,Authentication authentication,HttpServletRequest request){
        service.restore(caseId,authentication.getName(),clientIp(request));
        return "redirect:/cases/"+caseId;
    }

    private String clientIp(HttpServletRequest request){return request.getRemoteAddr();}
}
