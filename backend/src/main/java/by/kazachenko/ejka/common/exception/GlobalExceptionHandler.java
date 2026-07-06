package by.kazachenko.ejka.common.exception;

import by.kazachenko.ejka.common.dto.exception.ExceptionDto;
import by.kazachenko.ejka.common.dto.exception.Validation;
import by.kazachenko.ejka.common.exception.custom.AdditiveAlreadyExistsException;
import by.kazachenko.ejka.common.exception.custom.AdditiveNotFoundException;
import by.kazachenko.ejka.common.exception.custom.OriginAlreadyExistsException;
import by.kazachenko.ejka.common.exception.custom.OriginNotFoundException;
import by.kazachenko.ejka.common.exception.custom.ProductNotFoundException;
import by.kazachenko.ejka.common.exception.custom.ProductPendingException;
import by.kazachenko.ejka.common.exception.custom.ReviewAlreadyExistsException;
import by.kazachenko.ejka.common.exception.custom.TooManyPendingProductsException;
import by.kazachenko.ejka.common.exception.custom.UserAlreadyExistsException;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String FIELD_VALIDATION_FAILED = "Валидация полей неуспешна";
    private static final String HTTP_MESSAGE_NOT_READABLE_EXCEPTION = "Ошибка в формате JSON или неверный тип данных";


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

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<?> handleUsernameNotFoundException(UsernameNotFoundException e) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .build();
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> handleBadCredentialsException(BadCredentialsException e) {
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

    @ExceptionHandler(ReviewAlreadyExistsException.class)
    public ResponseEntity<ExceptionDto> handleReviewAlreadyExistsException(ReviewAlreadyExistsException e) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ExceptionDto(LocalDateTime.now(), e.getMessage(), null));
    }

    @ExceptionHandler(AdditiveAlreadyExistsException.class)
    public ResponseEntity<ExceptionDto> handleAdditiveAlreadyExistsException(AdditiveAlreadyExistsException e) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ExceptionDto(LocalDateTime.now(), e.getMessage(), null));
    }

    @ExceptionHandler(OriginAlreadyExistsException.class)
    public ResponseEntity<ExceptionDto> handleOriginAlreadyExistsException(OriginAlreadyExistsException e) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ExceptionDto(LocalDateTime.now(), e.getMessage(), null));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ExceptionDto> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ExceptionDto(LocalDateTime.now(), HTTP_MESSAGE_NOT_READABLE_EXCEPTION, null));
    }

    @ExceptionHandler({
            MalformedJwtException.class,
            SignatureException.class,
            UnsupportedJwtException.class
    })
    public ResponseEntity<ExceptionDto> handleInvalidJwtException(Exception e) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ExceptionDto(LocalDateTime.now(), "Невалидный токен", null));
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ExceptionDto> handleExpiredJwtException(ExpiredJwtException e) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ExceptionDto(LocalDateTime.now(), "Срок действия токена истек", null));
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<Void> handleAuthorizationDeniedException(AuthorizationDeniedException e) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .build();
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ExceptionDto> handleProductNotFoundException(ProductNotFoundException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ExceptionDto(LocalDateTime.now(), e.getMessage(), null));
    }

    @ExceptionHandler(AdditiveNotFoundException.class)
    public ResponseEntity<ExceptionDto> handleAdditiveNotFoundException(AdditiveNotFoundException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ExceptionDto(LocalDateTime.now(), e.getMessage(), null));
    }

    @ExceptionHandler(OriginNotFoundException.class)
    public ResponseEntity<ExceptionDto> handleOriginNotFoundException(OriginNotFoundException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ExceptionDto(LocalDateTime.now(), e.getMessage(), null));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ExceptionDto> handleAccessDeniedException(AccessDeniedException e) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ExceptionDto(LocalDateTime.now(), e.getMessage(), null));
    }

    @ExceptionHandler(InsufficientAuthenticationException.class)
    public ResponseEntity<ExceptionDto> handleAuthenticationException(InsufficientAuthenticationException e) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ExceptionDto(LocalDateTime.now(), e.getMessage(), null));
    }

    @ExceptionHandler(ProductPendingException.class)
    public ResponseEntity<ExceptionDto> handleProductPendingException(ProductPendingException e) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ExceptionDto(LocalDateTime.now(), e.getMessage(), null));
    }

    @ExceptionHandler(TooManyPendingProductsException.class)
    public ResponseEntity<ExceptionDto> handleTooManyPendingProductsException(TooManyPendingProductsException e) {
        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .body(new ExceptionDto(LocalDateTime.now(), e.getMessage(), null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionDto> handleException(Exception e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ExceptionDto(LocalDateTime.now(), e.getMessage(), null));
    }

}
