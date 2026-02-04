package by.kazachenko.ejka.user.service;

import by.kazachenko.ejka.user.dto.LoginRequest;
import by.kazachenko.ejka.user.dto.LoginResponse;
import by.kazachenko.ejka.user.dto.RegisterRequest;
import by.kazachenko.ejka.user.dto.RegisterResponse;

public interface AuthService {

    RegisterResponse register(RegisterRequest request);
    LoginResponse login(LoginRequest request);

}
