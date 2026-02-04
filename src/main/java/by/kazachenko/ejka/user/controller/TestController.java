package by.kazachenko.ejka.user.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TestController {

    @PreAuthorize("hasRole('MODERATOR')")
    @GetMapping("/moder/dashboard")
    public String adminDashboard() {
        return "Admin only";
    }

    @PreAuthorize("hasAnyRole('USER','MODERATOR')")
    @GetMapping("/user/profile")
    public String userProfile() {
        return "Accessed User profile";
    }
}
