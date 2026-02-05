package by.kazachenko.ejka.common.exception;

public record Validation(

   String fieldName,

   String message

) {
}
