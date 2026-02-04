package by.kazachenko.ejka.user.controller;

import by.kazachenko.ejka.user.dto.LoginRequest;
import by.kazachenko.ejka.user.dto.LoginResponse;
import by.kazachenko.ejka.user.dto.RegisterRequest;
import by.kazachenko.ejka.user.dto.RegisterResponse;
import by.kazachenko.ejka.user.service.JwtService;
import by.kazachenko.ejka.user.service.impl.AuthServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthServiceImpl authService;
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
