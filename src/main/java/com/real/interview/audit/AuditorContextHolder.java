package com.real.interview.audit;

import java.util.Optional;

public class AuditorContextHolder {
  private static final ThreadLocal<Long> AUDITOR = new ThreadLocal<>();

  public static Optional<Long> getAuditor() {
    return Optional.ofNullable(AUDITOR.get());
  }

  public static void setAuditor(Long userId) {
    AUDITOR.set(userId);
  }

  public static void clear() {
    AUDITOR.remove();
  }
}
