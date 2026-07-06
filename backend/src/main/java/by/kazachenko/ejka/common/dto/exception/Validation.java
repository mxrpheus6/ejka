package by.kazachenko.ejka.common.dto.exception;

public record Validation(

   String fieldName,

   String message

) {
}
