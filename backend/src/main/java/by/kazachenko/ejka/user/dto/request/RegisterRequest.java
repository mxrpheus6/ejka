package by.kazachenko.ejka.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record RegisterRequest(

    @NotBlank(message = "{auth_request.email.blank}")
    @Email(message = "{auth_request.email.invalid}")
    String email,

    @NotBlank(message = "{auth_request.password.blank}")
    @Size(min = 8, max = 16, message = "{auth_request.password.size}")
    String password,

    @NotBlank(message = "{auth_request.username.blank}")
    @Size(max = 32, message = "{auth_request.username.size}")
    @Pattern(
            regexp = "^[a-zA-Z0-9._]+$",
            message = "{auth_request.username.pattern}"
    )
    String username,

    @NotBlank(message = "{auth_request.name.blank}")
    @Size(max = 50, message = "{auth_request.name.size}")
    String name,

    @NotNull(message = "{auth_request.birth_date.null}")
    @Past(message = "{auth_request.birth_date.past}")
    LocalDate birthDate

) {
}
