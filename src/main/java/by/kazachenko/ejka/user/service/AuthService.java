package by.kazachenko.ejka.user.service;

import by.kazachenko.ejka.user.dto.request.RefreshTokenRequest;
import by.kazachenko.ejka.user.dto.response.AuthResponse;
import by.kazachenko.ejka.user.dto.request.LoginRequest;
import by.kazachenko.ejka.user.dto.request.RegisterRequest;

public interface AuthService {

    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refreshToken(RefreshTokenRequest refreshToken);
    void invalidateAllSessions(String email);

}
