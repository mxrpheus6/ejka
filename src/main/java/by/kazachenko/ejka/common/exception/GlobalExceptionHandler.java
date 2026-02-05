package by.kazachenko.ejka.common.exception;

import by.kazachenko.ejka.common.exception.cutom.UserAlreadyExistsException;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String FIELD_VALIDATION_FAILED = "Валидация полей неуспешна";


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionDto> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e) {
        List<Validation> validations = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> new Validation(
                        fieldError.getField(),
                        fieldError.getDefaultMessage()))
                .toList();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ExceptionDto(LocalDateTime.now(), FIELD_VALIDATION_FAILED, validations));
    }

    @ExceptionHandler({UsernameNotFoundException.class, BadCredentialsException.class})
    public ResponseEntity<?> handleUsernameNotFoundException(UsernameNotFoundException e) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .build();
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ExceptionDto> handleUserAlreadyExistsException(UserAlreadyExistsException e) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ExceptionDto(LocalDateTime.now(), e.getMessage(), null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionDto> handleException(Exception e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ExceptionDto(LocalDateTime.now(), e.getClass().toString(), null));
    }

}
