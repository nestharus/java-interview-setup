package com.real.interview.common.dto;

public interface VersionedDto<T> {
  long version();

  T id();
}
