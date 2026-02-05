package by.kazachenko.ejka.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(

        @NotBlank(message = "{auth_request.email.blank}")
        @Email(message = "{auth_request.email.invalid}")
        String email,

        @NotBlank(message = "{auth_request.password.blank}")
        @Size(min = 8, max = 16, message = "{auth_request.password.size}")
        String password

) {
}
