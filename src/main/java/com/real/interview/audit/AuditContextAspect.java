package com.real.interview.audit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Aspect that automatically populates the AuditorContextHolder with the username from the
 * X-Username HTTP header for all controller methods.
 *
 * <p>This ensures that audit fields (createdBy, lastModifiedBy) are properly populated during JPA
 * operations without requiring manual intervention in controller code.
 *
 * <p>Expected X-Username header values: - Authenticated users: actual username (e.g., "john.doe") -
 * Service-to-service calls: service name (e.g., "user-service", "notification-service") - Anonymous
 * users: no header (results in NULL audit fields)
 */
@Aspect
@Component
public class AuditContextAspect {

  /**
   * Intercepts all controller methods and populates the audit context with the username from the
   * X-Username header. Falls back to "system" if no username is provided.
   */
  @Around(
      "@within(org.springframework.web.bind.annotation.RestController) || "
          + "@within(org.springframework.stereotype.Controller)")
  public Object populateAuditContext(final ProceedingJoinPoint joinPoint) throws Throwable {
    final var username = extractUsernameFromRequest();

    AuditorContextHolder.setAuditor(username);

    try {
      return joinPoint.proceed();
    } catch (final Throwable exception) {
      AuditorContextHolder.clear();
      throw exception;
    }
    // Note: We don't clear the context here because the audit interceptor
    // needs to run after this method completes. The context will be cleared
    // by the request lifecycle or subsequent requests.
  }

  /**
   * Extracts the username from the X-Username HTTP header. Returns null if no header is present or
   * if it's empty, indicating an anonymous user.
   */
  private String extractUsernameFromRequest() {
    try {
      final var requestAttributes =
          (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

      if (requestAttributes != null) {
        final var request = requestAttributes.getRequest();
        final var username = request.getHeader("X-Username");

        if (username != null && !username.trim().isEmpty()) {
          return username.trim();
        }
      }
    } catch (final Exception exception) {
      // Log the exception if needed, but don't fail the request
    }

    // Return null for anonymous users (public endpoints with no authentication)
    return null;
  }
}
