package by.kazachenko.ejka.user.dto.response;

import by.kazachenko.ejka.user.model.enums.Role;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String username,
        String name,
        String avatarKey,
        String email,
        Role role,
        LocalDate birthDate,
        Instant registrationDate,
        Boolean isBanned,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String banReason
) {

}
