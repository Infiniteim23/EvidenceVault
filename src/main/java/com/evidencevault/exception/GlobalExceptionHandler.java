package com.evidencevault.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    String size(Model m){m.addAttribute("message","Uploaded file exceeds the maximum allowed size.");return "error/generic";}

    @ExceptionHandler(IllegalArgumentException.class)
    String badRequest(IllegalArgumentException e, Model m){m.addAttribute("message",safeMessage(e,"Invalid request."));return "error/generic";}

    @ExceptionHandler(Exception.class)
    String generic(Exception e,Model m){
        log.error("Unhandled application exception", e);
        m.addAttribute("message","Operation failed. Please retry or contact an administrator.");
        return "error/generic";
    }

    private String safeMessage(Exception e, String fallback) {
        String message=e.getMessage();
        if(message==null || message.isBlank() || message.length()>300) return fallback;
        return message;
    }
}
