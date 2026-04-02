package by.kazachenko.ejka.user.controller;

import by.kazachenko.ejka.user.dto.response.UserResponse;

import by.kazachenko.ejka.user.service.UserService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<UserResponse> getUserProfile() {
        return ResponseEntity.ok(userService.getUserProfile());
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteUserProfile() {
        userService.deleteUserProfile();

        return ResponseEntity.noContent().build();
    }

}
