package com.real.interview.audit;

import jakarta.annotation.Nonnull;
import java.util.Optional;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class AuditorProvider implements AuditorAware<String> {
  @Override
  @Nonnull
  public Optional<String> getCurrentAuditor() {
    // First try to get from AuditorContextHolder (populated by aspect)
    final var contextAuditor = AuditorContextHolder.getAuditor().orElse(null);
    if (contextAuditor != null) {
      return Optional.of(contextAuditor);
    }

    // Fallback to direct header access (for cases where aspect doesn't run)
    final var headerUsername =
        Optional.ofNullable(RequestContextHolder.getRequestAttributes())
            .filter(ServletRequestAttributes.class::isInstance)
            .map(ServletRequestAttributes.class::cast)
            .map(ServletRequestAttributes::getRequest)
            .map(request -> request.getHeader("X-Username"))
            .filter(header -> !header.trim().isEmpty())
            .map(String::trim)
            .orElse(null);

    // Return Optional.empty() for anonymous users (null username)
    // This will result in NULL values in the database audit fields
    return Optional.ofNullable(headerUsername);
  }
}
