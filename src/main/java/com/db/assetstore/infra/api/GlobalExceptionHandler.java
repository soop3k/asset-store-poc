package com.db.assetstore.infra.api;

import com.db.assetstore.domain.exception.DomainException;
import com.db.assetstore.domain.exception.JsonException;
import com.db.assetstore.domain.exception.TransformSchemaValidationException;
import com.db.assetstore.domain.exception.TransformTemplateNotFoundException;
import com.db.assetstore.domain.exception.command.CommandException;
import com.db.assetstore.domain.exception.link.ActiveLinkNotFoundException;
import com.db.assetstore.domain.exception.link.InactiveLinkDefinitionException;
import com.db.assetstore.domain.exception.link.LinkAlreadyExistsException;
import com.db.assetstore.domain.exception.link.LinkCardinalityViolationException;
import com.db.assetstore.domain.exception.link.LinkDefinitionNotFoundException;
import com.db.assetstore.domain.exception.link.LinkException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, WebRequest request) {
        return respond(HttpStatus.BAD_REQUEST, ex.getMessage(), request, false, ex);
    }

    @ExceptionHandler({LinkDefinitionNotFoundException.class, ActiveLinkNotFoundException.class,
            TransformTemplateNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFound(DomainException ex, WebRequest request) {
        return respond(HttpStatus.NOT_FOUND, ex.getMessage(), request, false, ex);
    }

    @ExceptionHandler({LinkAlreadyExistsException.class, LinkCardinalityViolationException.class,
            InactiveLinkDefinitionException.class})
    public ResponseEntity<ErrorResponse> handleConflict(LinkException ex, WebRequest request) {
        return respond(HttpStatus.CONFLICT, ex.getMessage(), request, false, ex);
    }

    @ExceptionHandler(TransformSchemaValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(TransformSchemaValidationException ex, WebRequest request) {
        return respond(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), request, false, ex);
    }

    @ExceptionHandler(CommandException.class)
    public ResponseEntity<ErrorResponse> handleCommand(CommandException ex, WebRequest request) {
        return respond(HttpStatus.CONFLICT, ex.getMessage(), request, false, ex);
    }

    @ExceptionHandler(JsonException.class)
    public ResponseEntity<ErrorResponse> handleJson(JsonException ex, WebRequest request) {
        return respond(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request, true, ex);
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDomainFallback(DomainException ex, WebRequest request) {
        return respond(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request, true, ex);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, WebRequest request) {
        return respond(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", request, true, ex);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers,
                                                                  HttpStatusCode status, WebRequest request) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));
        ResponseEntity<ErrorResponse> response = respond(HttpStatus.BAD_REQUEST, details, request, false, ex);
        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }

    private ResponseEntity<ErrorResponse> respond(HttpStatus status, String message, WebRequest request,
                                                  boolean errorLevel, Exception ex) {
        String path = request.getDescription(false).replace("uri=", "");
        if (errorLevel) {
            log.error("{} {} at {}: {}", status.value(), status.getReasonPhrase(), path, message, ex);
        } else {
            log.warn("{} {} at {}: {}", status.value(), status.getReasonPhrase(), path, message);
        }
        ErrorResponse body = new ErrorResponse(status.value(), status.getReasonPhrase(), message, path);
        return ResponseEntity.status(status).body(body);
    }
}
