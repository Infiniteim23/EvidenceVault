package com.evidencevault.controller; import org.springframework.stereotype.Controller; import org.springframework.web.bind.annotation.GetMapping;
@Controller public class AuthController { @GetMapping("/login") String login(){return "login";} @GetMapping("/403") String forbidden(){return "error/403";} }
