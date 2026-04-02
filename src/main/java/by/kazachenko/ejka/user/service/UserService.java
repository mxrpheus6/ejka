package by.kazachenko.ejka.user.service;

import by.kazachenko.ejka.user.dto.request.UserRequest;
import by.kazachenko.ejka.user.dto.response.UserResponse;
import java.util.UUID;

public interface UserService {

    UserResponse getUserProfile();

    UserResponse updateUser(UUID userId, UserRequest request);

    void deleteUserProfile();

}
