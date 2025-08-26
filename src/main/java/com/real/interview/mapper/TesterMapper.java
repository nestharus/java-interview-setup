package com.real.interview.mapper;

import com.real.interview.common.entity.AbstractEntity;
import com.real.interview.domain.tester.Tester;
import com.real.interview.dto.tester.TesterDto;
import com.real.interview.mapper.common.ToDto;
import com.real.interview.mapper.common.ToEntity;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TesterMapper {

  @ToDto
  TesterDto toDto(Tester tester);

  default void updateEntities(
      Collection<TesterDto> dtos, @MappingTarget Collection<Tester> entities) {
    final var entityById =
        entities.stream().collect(Collectors.toMap(AbstractEntity::getId, Function.identity()));

    dtos.forEach(dto -> updateEntity(dto, entityById.get(dto.id())));
  }

  @ToEntity
  @Mapping(target = "tester2List", ignore = true)
  void updateEntity(TesterDto dto, @MappingTarget Tester entity);

  @InheritConfiguration(name = "updateEntity")
  Tester toEntity(TesterDto dto);
}
