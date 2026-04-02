package by.kazachenko.ejka.user.dto.response;

import by.kazachenko.ejka.user.model.enums.Role;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String username,
        String email,
        Role role
) {

}
