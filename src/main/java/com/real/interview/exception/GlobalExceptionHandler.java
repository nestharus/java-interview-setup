package com.real.interview.exception;

import com.real.interview.common.entity.AbstractEntity;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptionHandler {
  private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(EntityBulkUpdateException.class)
  public ResponseEntity<Object> handleEntityBulkUpdateException(
      final EntityBulkUpdateException exception, final WebRequest webRequest) {
    final var foundIds =
        exception.getFoundEntities().stream()
            .map(AbstractEntity::getId)
            .collect(Collectors.toSet());
    final var missingIds =
        exception.getSearchedIds().stream().filter(id -> !foundIds.contains(id)).toList();

    logger.error("Entities not found: {}", "%s".formatted(missingIds), exception);
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(missingIds);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Object> handleIllegalArgumentException(final IllegalArgumentException exception, final WebRequest webRequest) {
    logger.error("Bad request: Invalid argument provided", exception);
    return ResponseEntity.badRequest().body(exception.getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Object> handleIllegalArgumentException(final MethodArgumentNotValidException exception, final WebRequest webRequest) {
    final var errorMessage = exception.getBindingResult().getFieldErrors().stream()
        .map(error -> error.getField() + ": " + error.getDefaultMessage())
        .collect(Collectors.joining(", "));
    logger.error("Validation failed: {}", errorMessage, exception);
    return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);

  }

  @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
  public ResponseEntity<Object> handleOptimisticLockingFailureException(final ObjectOptimisticLockingFailureException exception, final WebRequest webRequest) {
    logger.warn("Optimistic locking failure: A concurrent modification was detected", exception);
    return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflict: The data was modified by another user. Please refresh and try again.");
  }

  @ExceptionHandler(NoSuchElementException.class)
  public ResponseEntity<Object> handleNoSuchElementException(final NoSuchElementException exception, final WebRequest webRequest) {
    logger.error("Resource not found: {}", exception.getMessage(), exception);
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("The requested resource could not be found.");
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<Object> handleDataIntegrityViolationException(final DataIntegrityViolationException exception, final WebRequest webRequest) {
    logger.error("Database integrity violation: {}", exception.getMessage(), exception);
    return ResponseEntity.status(HttpStatus.CONFLICT).body("Data conflict or integrity violation. Please check your input.");
  }

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<Object> handleIllegalArgumentException(final RuntimeException exception, final WebRequest webRequest) {
    logger.error("An unexpected error occurred", exception);
    return ResponseEntity.internalServerError().body(exception.getMessage());
  }
}
