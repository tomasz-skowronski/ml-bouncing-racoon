package de.magicline.racoon.config;

import de.magicline.racoon.api.dto.ErrorResponse;
import javax.validation.ValidationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

@ControllerAdvice
public class CustomGlobalExceptionHandler {

    private static final Logger LOGGER = LogManager.getLogger(CustomGlobalExceptionHandler.class);

    @ExceptionHandler(ResponseStatusException.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleResponseStatusException(ResponseStatusException e) {
        LOGGER.warn(e.getMessage(), e);
        return ResponseEntity.status(e.getStatus())
                .body(new ErrorResponse(e));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        LOGGER.info("", e);
        return toResponse(e, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({ValidationException.class, MethodArgumentNotValidException.class})
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleValidationException(Exception e) {
        LOGGER.info("", e);
        return toResponse(e, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        LOGGER.error("", e);
        return toResponse(e, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ErrorResponse> toResponse(Exception e, HttpStatus internalServerError) {
        return ResponseEntity.status(internalServerError)
                .body(new ErrorResponse(internalServerError.value(), e.getMessage()));
    }

}
