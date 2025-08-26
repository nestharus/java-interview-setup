package com.real.interview.common.entity;

public interface VersionedEntity<T> {
  long getVersion();

  T getId();
}
