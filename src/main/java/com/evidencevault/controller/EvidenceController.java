package com.evidencevault.controller;

import com.evidencevault.entity.Evidence;
import com.evidencevault.entity.EvidenceVerification;
import com.evidencevault.service.EvidenceService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@Controller
@RequestMapping("/evidence")
public class EvidenceController {
    private final EvidenceService service;
    private final com.evidencevault.service.AuditService audit;
    public EvidenceController(EvidenceService service, com.evidencevault.service.AuditService audit){this.service=service;this.audit=audit;}

    @GetMapping("/upload/{caseId}")
    public String upload(@PathVariable String caseId,Authentication authentication,HttpServletRequest request,Model model){
        // Access is checked before displaying the upload form as well as during POST.
        var evidenceCase = service.getCaseAuthorized(caseId, authentication.getName(), request.getRemoteAddr());
        if ("ARCHIVED".equalsIgnoreCase(evidenceCase.getStatus())) {
            model.addAttribute("error", "This case is archived. Restore it before uploading evidence.");
        }
        model.addAttribute("caseId",caseId);
        model.addAttribute("caseTitle",evidenceCase.getTitle());
        return "evidence/upload";
    }

    @PostMapping("/upload/{caseId}")
    public String upload(@PathVariable String caseId,@RequestParam("file") MultipartFile file,@RequestParam(defaultValue="") String description,
                         Authentication authentication,HttpServletRequest request,Model model){
        try{
            Evidence evidence=service.upload(caseId,file,description,authentication.getName(),request.getRemoteAddr());
            return "redirect:/evidence/"+evidence.getId();
        }catch(Exception e){
            model.addAttribute("caseId",caseId); model.addAttribute("error",e.getMessage());
            return "evidence/upload";
        }
    }

    @GetMapping("/{id}")
    public String details(@PathVariable Long id,Model model,Authentication authentication,HttpServletRequest request){
        Evidence evidence=service.getAuthorized(id,authentication.getName(),request.getRemoteAddr(),"EVIDENCE_VIEWED");
        model.addAttribute("evidence",evidence); model.addAttribute("history",service.history(id));
        model.addAttribute("timeline",audit.forEntity("EVIDENCE",String.valueOf(id)));
        return "evidence/details";
    }

    @PostMapping("/{id}/verify")
    public String verify(@PathVariable Long id,Authentication authentication,HttpServletRequest request,Model model){
        EvidenceVerification verification=service.verify(id,authentication.getName(),request.getRemoteAddr());
        Evidence evidence=service.getAuthorized(id,authentication.getName(),request.getRemoteAddr(),"EVIDENCE_VIEWED");
        model.addAttribute("evidence",evidence); model.addAttribute("verification",verification);
        return "evidence/result";
    }

    @GetMapping("/{id}/history")
    public String history(@PathVariable Long id,Model model,Authentication authentication,HttpServletRequest request){
        Evidence evidence=service.getAuthorized(id,authentication.getName(),request.getRemoteAddr(),"EVIDENCE_HISTORY_VIEWED");
        model.addAttribute("evidence",evidence); model.addAttribute("history",service.history(id));
        return "evidence/history";
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id,Authentication authentication,HttpServletRequest request) throws Exception{
        Evidence evidence=service.getAuthorized(id,authentication.getName(),request.getRemoteAddr(),"EVIDENCE_DOWNLOAD");
        File file=service.file(evidence);
        if(!file.exists()||!file.isFile()) return ResponseEntity.notFound().build();
        service.recordDownload(evidence,authentication.getName(),request.getRemoteAddr());
        Resource resource=new FileSystemResource(file);
        MediaType mediaType;
        try{mediaType=MediaType.parseMediaType(evidence.getMimeType());}catch(Exception ignored){mediaType=MediaType.APPLICATION_OCTET_STREAM;}
        String safeFilename=evidence.getOriginalFilename().replaceAll("[\\r\\n\"/\\\\]", "_");
        return ResponseEntity.ok().contentType(mediaType).contentLength(file.length())
                .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\""+safeFilename+"\"").body(resource);
    }
}
