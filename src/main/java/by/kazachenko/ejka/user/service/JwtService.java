package by.kazachenko.ejka.user.service;

import by.kazachenko.ejka.user.model.User;
import io.jsonwebtoken.Claims;

public interface JwtService {

    String generateAccessToken(User user);

    String generateRefreshToken(User user);

    String extractUsername(String token);

    boolean isTokenValid(String token);

    Claims extractAllClaims(String token);

}
