package com.real.interview.common.validator;

import com.real.interview.common.dto.VersionedDto;
import com.real.interview.common.entity.AbstractEntity;
import com.real.interview.common.entity.VersionedEntity;
import com.real.interview.exception.EntityBulkUpdateException;
import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

public class EntityValidator {
  public static void validateAllEntitiesFound(
      final Collection<Long> searchedIds,
      final Collection<? extends AbstractEntity> foundEntities) {
    if (searchedIds.size() != foundEntities.size()) {
      throw new EntityBulkUpdateException(searchedIds, foundEntities);
    }
  }

  public static void validateIsNotStale(final VersionedEntity<?> entity, final long version) {
    if (entity.getVersion() != version) {
      throw new ObjectOptimisticLockingFailureException(entity.getClass(), entity.getId());
    }
  }

  public static <ID, DTO extends VersionedDto<ID>, E extends VersionedEntity<ID>>
      BiConsumer<Collection<DTO>, Collection<E>> withEntitiesLocking(
          final BiConsumer<DTO, E> updater) {
    return (Collection<DTO> dtos, Collection<E> entities) -> {
      final var entityById =
          entities.stream().collect(Collectors.toMap(VersionedEntity::getId, Function.identity()));

      dtos.forEach(
          dto -> {
            final var entity = entityById.get(dto.id());

            validateIsNotStale(entity, dto.version());

            updater.accept(dto, entity);
          });
    };
  }

  public static <ID, DTO extends VersionedDto<ID>, E extends VersionedEntity<ID>>
      BiConsumer<DTO, E> withEntityLocking(final BiConsumer<DTO, E> updater) {
    return (DTO dto, E entity) -> {
      validateIsNotStale(entity, dto.version());

      updater.accept(dto, entity);
    };
  }
}
