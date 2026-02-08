package by.kazachenko.ejka.common.dto.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.time.LocalDateTime;
import java.util.List;

public record ExceptionDto(

        LocalDateTime timestamp,

        String message,

        @JsonInclude(Include.NON_NULL)
        List<Validation> validations

) {
}
