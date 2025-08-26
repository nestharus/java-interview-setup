package com.real.interview.exception;

import com.real.interview.common.entity.AbstractEntity;
import java.util.Collection;
import lombok.Getter;

@Getter
public class EntityBulkUpdateException extends RuntimeException {
  private final Collection<Long> searchedIds;
  private final Collection<? extends AbstractEntity> foundEntities;

  public EntityBulkUpdateException(
      final Collection<Long> searchedIds,
      final Collection<? extends AbstractEntity> foundEntities,
      final Throwable cause) {
    super(cause);

    this.searchedIds = searchedIds;
    this.foundEntities = foundEntities;
  }

  public EntityBulkUpdateException(
      final Collection<Long> searchedIds,
      final Collection<? extends AbstractEntity> foundEntities) {
    super();

    this.searchedIds = searchedIds;
    this.foundEntities = foundEntities;
  }
}
