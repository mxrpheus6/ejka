package by.kazachenko.ejka.user.service;

import by.kazachenko.ejka.user.dto.request.UserRequest;
import by.kazachenko.ejka.user.dto.response.UserResponse;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {

    UserResponse getUserProfile();

    UserResponse updateUser(UUID userId, UserRequest request);

    void deleteUserProfile();

    void uploadAvatar(MultipartFile file);

    void deleteAvatar();

}
