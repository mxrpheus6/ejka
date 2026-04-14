package by.kazachenko.ejka.user.controller;

import by.kazachenko.ejka.user.dto.response.UserResponse;
import by.kazachenko.ejka.user.service.UserService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getUserProfile() {
        return ResponseEntity.ok(userService.getUserProfile());
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteUserProfile() {
        userService.deleteUserProfile();

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/me/avatar")
    public ResponseEntity<Void> uploadAvatar(@RequestPart("file") MultipartFile file) {
        userService.uploadAvatar(file);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/me/avatar")
    public ResponseEntity<Void> deleteAvater() {
        userService.deleteAvatar();

        return ResponseEntity.noContent().build();
    }

}
