package in.expensebudget.controller;

import in.expensebudget.dto.ApiDtos;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public org.springframework.http.ResponseEntity<ApiDtos.MessageResponse> invalidInput(
            MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst().map(error -> error.getField() + " " +
                        Objects.requireNonNullElse(error.getDefaultMessage(), "is invalid"))
                .orElse("Please check the submitted information.");
        return org.springframework.http.ResponseEntity.badRequest().body(new ApiDtos.MessageResponse(message));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public org.springframework.http.ResponseEntity<ApiDtos.MessageResponse> unreadableInput(
            HttpMessageNotReadableException exception) {
        return org.springframework.http.ResponseEntity.badRequest()
                .body(new ApiDtos.MessageResponse("The request contains invalid data."));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public org.springframework.http.ResponseEntity<ApiDtos.MessageResponse> statusError(
            ResponseStatusException exception) {
        return org.springframework.http.ResponseEntity.status(exception.getStatusCode())
                .body(new ApiDtos.MessageResponse(Objects.requireNonNullElse(
                        exception.getReason(), "The request could not be completed.")));
    }
}
