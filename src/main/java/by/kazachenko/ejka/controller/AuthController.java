package by.kazachenko.ejka.controller;

import by.kazachenko.ejka.dto.LoginRequest;
import by.kazachenko.ejka.dto.LoginResponse;
import by.kazachenko.ejka.dto.RegisterRequest;
import by.kazachenko.ejka.dto.RegisterResponse;
import by.kazachenko.ejka.security.JwtService;
import by.kazachenko.ejka.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    @PostMapping("/register")
    public RegisterResponse register(@RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
