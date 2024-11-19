package pl.kamann.config.exception.utility;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletResponse;
import lombok.experimental.UtilityClass;
import org.springframework.http.HttpStatus;
import pl.kamann.config.exception.response.ErrorResponse;

import java.io.IOException;
import java.time.LocalDateTime;

@UtilityClass
public class SecurityErrorResponseUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public static void writeJsonResponse(
            HttpServletResponse response,
            HttpStatus status,
            String errorCode,
            String message
    ) throws IOException {
        ErrorResponse error = new ErrorResponse(
                status.value(),
                errorCode,
                message,
                LocalDateTime.now()
        );

        response.setContentType("application/json");
        response.setStatus(status.value());
        response.getWriter().write(objectMapper.writeValueAsString(error));
    }
}