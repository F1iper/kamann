package pl.kamann.config.exception.handler;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import pl.kamann.config.exception.response.ErrorResponse;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException ex) {
        ErrorResponse error = new ErrorResponse(
                ex.getStatus().value(),
                ex.getCode(),
                ex.getMessage(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, ex.getStatus());
    }
}