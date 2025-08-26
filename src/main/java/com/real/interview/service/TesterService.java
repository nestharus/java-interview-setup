package com.real.interview.service;

import static com.real.interview.common.validator.EntityValidator.*;

import com.real.interview.domain.tester.Tester;
import com.real.interview.dto.tester.TesterDto;
import com.real.interview.mapper.TesterMapper;
import com.real.interview.repository.TesterRepository;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class TesterService {
  private static final Logger logger = org.slf4j.LoggerFactory.getLogger(TesterService.class);

  private final TesterRepository testerRepository;
  private final TesterMapper testerMapper;

  @Transactional(readOnly = true)
  public Optional<TesterDto> getTest(final long id) {
    return testerRepository.findById(id).map(testerMapper::toDto);
  }

  @Transactional
  public List<TesterDto> updateTesters(final List<TesterDto> dtos) {
    if (dtos == null) {
      throw new IllegalArgumentException("Cannot update null tester list");
    }

    if (dtos.contains(null)) {
      throw new IllegalArgumentException("Cannot update null tester");
    }

    final var partitioned =
        dtos.stream().collect(Collectors.partitioningBy(dto -> dto.id() != null));

    final var dtosToUpdate = partitioned.get(true);
    final var dtosToCreate = partitioned.get(false);

    final var updatedEntities = updateExistingTesters(dtosToUpdate);
    final var createdEntities = createNewTesters(dtosToCreate);

    return Stream.of(updatedEntities, createdEntities)
        .flatMap(List::stream)
        .map(testerMapper::toDto)
        .toList();
  }

  public List<Tester> updateExistingTesters(final List<TesterDto> dtosWithUpdates) {
    if (dtosWithUpdates.isEmpty()) {
      return List.of();
    }

    final var idsToSearch = dtosWithUpdates.stream().map(TesterDto::id).toList();

    final var entitiesToUpdate = testerRepository.findAllById(idsToSearch);

    validateAllEntitiesFound(idsToSearch, entitiesToUpdate);
    withEntitiesLocking(testerMapper::updateEntity).accept(dtosWithUpdates, entitiesToUpdate);

    return testerRepository.saveAll(entitiesToUpdate);
  }

  public List<Tester> createNewTesters(final List<TesterDto> newDtos) {
    if (newDtos.isEmpty()) {
      return List.of();
    }

    final var entitiesToCreate = newDtos.stream().map(testerMapper::toEntity).toList();

    return testerRepository.saveAll(entitiesToCreate);
  }
}
