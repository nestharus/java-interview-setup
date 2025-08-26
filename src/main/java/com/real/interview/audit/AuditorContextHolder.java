package com.real.interview.audit;

import java.util.Optional;

public class AuditorContextHolder {
  private static final ThreadLocal<String> AUDITOR = new ThreadLocal<>();

  public static Optional<String> getAuditor() {
    return Optional.ofNullable(AUDITOR.get());
  }

  public static void setAuditor(final String username) {
    AUDITOR.set(username);
  }

  public static void clear() {
    AUDITOR.remove();
  }
}
