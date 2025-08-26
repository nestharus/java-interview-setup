package com.real.interview.audit;

import jakarta.annotation.Nonnull;
import java.util.Optional;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component("auditorProvider")
public class AuditorProvider implements AuditorAware<Long> {
  @Override
  @Nonnull
  public Optional<Long> getCurrentAuditor() {
    return AuditorContextHolder.getAuditor()
        .or(
            () ->
                Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                    .filter(ServletRequestAttributes.class::isInstance)
                    .map(ServletRequestAttributes.class::cast)
                    .map(ServletRequestAttributes::getRequest)
                    .map(request -> request.getHeader("X-User-ID"))
                    .filter(header -> !header.isEmpty())
                    .map(Long::parseLong));
  }
}
