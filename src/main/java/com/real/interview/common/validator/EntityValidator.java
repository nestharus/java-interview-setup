package com.real.interview.common.validator;

import com.real.interview.common.entity.AbstractEntity;
import com.real.interview.exception.EntityBulkUpdateException;
import java.util.Collection;

public class EntityValidator {
  public static void validateAllEntitiesFound(
      final Collection<Long> searchedIds,
      final Collection<? extends AbstractEntity> foundEntities) {
    if (searchedIds.size() != foundEntities.size()) {
      throw new EntityBulkUpdateException(searchedIds, foundEntities);
    }
  }
}
