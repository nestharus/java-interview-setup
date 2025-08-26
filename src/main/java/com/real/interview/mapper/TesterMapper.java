package com.real.interview.mapper;

import com.real.interview.domain.tester.Tester;
import com.real.interview.dto.tester.TesterDto;
import com.real.interview.mapper.common.ToDto;
import com.real.interview.mapper.common.ToEntity;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TesterMapper {

  @ToDto
  TesterDto toDto(Tester tester);

  @ToEntity
  @Mapping(target = "tester2List", ignore = true)
  void updateEntity(TesterDto dto, @MappingTarget Tester entity);

  @InheritConfiguration(name = "updateEntity")
  Tester toEntity(TesterDto dto);
}
