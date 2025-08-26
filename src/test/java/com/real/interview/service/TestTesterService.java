package com.real.interview.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

import com.real.interview.domain.tester.Tester;
import com.real.interview.domain.tester.TesterTagType;
import com.real.interview.dto.tester.TesterDto;
import com.real.interview.exception.EntityBulkUpdateException;
import com.real.interview.mapper.TesterMapper;
import com.real.interview.repository.TesterRepository;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("TesterService Component Tests")
class TestTesterService {

  @MockitoBean private TesterRepository testerRepository;

  @Autowired private TesterMapper testerMapper;

  @Autowired private TesterService testerService;

  private Tester sampleTester;
  private TesterDto sampleTesterDto;

  @BeforeEach
  void setUp() {
    sampleTester = new Tester();
    sampleTester.setId(1L);
    sampleTester.setName("Test Tester");
    sampleTester.setTags(Set.of(TesterTagType.TAG1));
    sampleTester.setVersion(0L);

    sampleTesterDto =
        TesterDto.builder()
            .id(1L)
            .name("Test Tester")
            .tags(Set.of(TesterTagType.TAG1))
            .version(0L)
            .build();
  }

  @Test
  @DisplayName("getTest - Should return tester DTO when found")
  void getTest_WhenTesterExists_ShouldReturnDto() {
    when(testerRepository.findById(1L)).thenReturn(Optional.of(sampleTester));

    Optional<TesterDto> result = testerService.getTest(1L);

    assertThat(result).isPresent();
    assertThat(result.get().id()).isEqualTo(1L);
    assertThat(result.get().name()).isEqualTo("Test Tester");
    assertThat(result.get().tags()).containsExactly(TesterTagType.TAG1);
    assertThat(result.get().version()).isEqualTo(0L);
    verify(testerRepository).findById(1L);
  }

  @Test
  @DisplayName("getTest - Should return empty when not found")
  void getTest_WhenTesterNotFound_ShouldReturnEmpty() {
    when(testerRepository.findById(999L)).thenReturn(Optional.empty());

    Optional<TesterDto> result = testerService.getTest(999L);

    assertThat(result).isEmpty();
    verify(testerRepository).findById(999L);
  }

  @Test
  @DisplayName("updateTesters - Should throw exception for null list")
  void updateTesters_WithNullList_ShouldThrowException() {
    assertThatThrownBy(() -> testerService.updateTesters(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Cannot update null tester list");

    verifyNoInteractions(testerRepository);
  }

  @Test
  @DisplayName("updateTesters - Should throw exception for list containing null")
  void updateTesters_WithListContainingNull_ShouldThrowException() {
    List<TesterDto> dtos = new ArrayList<>();
    dtos.add(sampleTesterDto);
    dtos.add(null);

    assertThatThrownBy(() -> testerService.updateTesters(dtos))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Cannot update null tester");

    verifyNoInteractions(testerRepository);
  }

  @Test
  @DisplayName("updateTesters - Should handle empty list")
  void updateTesters_WithEmptyList_ShouldReturnEmptyList() {
    List<TesterDto> result = testerService.updateTesters(new ArrayList<>());

    assertThat(result).isEmpty();
    verifyNoInteractions(testerRepository);
  }

  @Test
  @DisplayName("updateTesters - Should update existing testers")
  void updateTesters_WithExistingTesters_ShouldUpdate() {
    TesterDto dto1 =
        TesterDto.builder()
            .id(1L)
            .name("Updated Tester 1")
            .tags(Set.of(TesterTagType.TAG1))
            .version(1L)
            .build();

    TesterDto dto2 =
        TesterDto.builder()
            .id(2L)
            .name("Updated Tester 2")
            .tags(Set.of(TesterTagType.TAG2))
            .version(0L)
            .build();

    Tester entity1 = new Tester();
    entity1.setId(1L);
    entity1.setName("Original Tester 1");
    entity1.setVersion(1L);
    entity1.setTags(new HashSet<>(Set.of(TesterTagType.TAG3)));

    Tester entity2 = new Tester();
    entity2.setId(2L);
    entity2.setName("Original Tester 2");
    entity2.setVersion(0L);
    entity2.setTags(new HashSet<>(Set.of(TesterTagType.TAG4)));

    when(testerRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(entity1, entity2));
    when(testerRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

    List<TesterDto> result = testerService.updateTesters(new ArrayList<>(List.of(dto1, dto2)));

    assertThat(result).hasSize(2);
    assertThat(result.get(0).id()).isEqualTo(1L);
    assertThat(result.get(0).name()).isEqualTo("Updated Tester 1");
    assertThat(result.get(0).tags()).containsExactly(TesterTagType.TAG1);
    assertThat(result.get(1).id()).isEqualTo(2L);
    assertThat(result.get(1).name()).isEqualTo("Updated Tester 2");
    assertThat(result.get(1).tags()).containsExactly(TesterTagType.TAG2);

    verify(testerRepository).findAllById(List.of(1L, 2L));
    verify(testerRepository).saveAll(anyList());
  }

  @Test
  @DisplayName("updateTesters - Should create new testers")
  void updateTesters_WithNewTesters_ShouldCreate() {
    TesterDto newDto1 =
        TesterDto.builder()
            .id(null)
            .name("New Tester 1")
            .tags(Set.of(TesterTagType.TAG1))
            .version(0L)
            .build();

    TesterDto newDto2 =
        TesterDto.builder()
            .id(null)
            .name("New Tester 2")
            .tags(Set.of(TesterTagType.TAG2))
            .version(0L)
            .build();

    when(testerRepository.saveAll(anyList()))
        .thenAnswer(
            invocation -> {
              List<Tester> entities = invocation.getArgument(0);
              long id = 100L;
              for (Tester entity : entities) {
                entity.setId(id++);
              }
              return entities;
            });

    List<TesterDto> result =
        testerService.updateTesters(new ArrayList<>(List.of(newDto1, newDto2)));

    assertThat(result).hasSize(2);
    assertThat(result.get(0).id()).isEqualTo(100L);
    assertThat(result.get(0).name()).isEqualTo("New Tester 1");
    assertThat(result.get(0).tags()).containsExactly(TesterTagType.TAG1);
    assertThat(result.get(1).id()).isEqualTo(101L);
    assertThat(result.get(1).name()).isEqualTo("New Tester 2");
    assertThat(result.get(1).tags()).containsExactly(TesterTagType.TAG2);

    verify(testerRepository).saveAll(anyList());
  }

  @Test
  @DisplayName("updateTesters - Should handle mixed create and update")
  void updateTesters_WithMixedOperations_ShouldProcessBoth() {
    TesterDto existingDto =
        TesterDto.builder()
            .id(1L)
            .name("Existing Tester")
            .tags(Set.of(TesterTagType.TAG2))
            .version(1L)
            .build();

    TesterDto newDto =
        TesterDto.builder()
            .id(null)
            .name("New Tester")
            .tags(Set.of(TesterTagType.TAG1))
            .version(0L)
            .build();

    Tester existingEntity = new Tester();
    existingEntity.setId(1L);
    existingEntity.setName("Old Name");
    existingEntity.setVersion(1L);
    existingEntity.setTags(new HashSet<>());

    when(testerRepository.findAllById(List.of(1L))).thenReturn(List.of(existingEntity));

    // Mock saveAll to return the same entities for existing updates
    when(testerRepository.saveAll(
            argThat(
                list -> {
                  if (list instanceof List<?> l) {
                    Tester t = (Tester) l.get(0);
                    return !l.isEmpty() && t.getId() != 0 && t.getId() == 1L;
                  }
                  return false;
                })))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // Mock saveAll to set IDs for new entities
    when(testerRepository.saveAll(
            argThat(
                list -> {
                  if (list instanceof List<?> l) {
                    Tester t = (Tester) l.get(0);
                    return !l.isEmpty() && t.getId() == 0L;
                  }
                  return false;
                })))
        .thenAnswer(
            invocation -> {
              List<Tester> entities = invocation.getArgument(0);
              entities.get(0).setId(100L);
              return entities;
            });

    List<TesterDto> result =
        testerService.updateTesters(new ArrayList<>(List.of(existingDto, newDto)));

    assertThat(result).hasSize(2);

    // Check updated entity
    Optional<TesterDto> updatedDto = result.stream().filter(dto -> dto.id() == 1L).findFirst();
    assertThat(updatedDto).isPresent();
    assertThat(updatedDto.get().name()).isEqualTo("Existing Tester");
    assertThat(updatedDto.get().tags()).containsExactly(TesterTagType.TAG2);

    // Check new entity
    Optional<TesterDto> savedNewDto = result.stream().filter(dto -> dto.id() == 100L).findFirst();
    assertThat(savedNewDto).isPresent();
    assertThat(savedNewDto.get().name()).isEqualTo("New Tester");
    assertThat(savedNewDto.get().tags()).containsExactly(TesterTagType.TAG1);

    verify(testerRepository).findAllById(List.of(1L));
    verify(testerRepository, times(2)).saveAll(anyList());
  }

  @Test
  @DisplayName("updateTesters - Should throw exception when entity not found for update")
  void updateTesters_WhenEntityNotFound_ShouldThrowException() {
    TesterDto dto = TesterDto.builder().id(999L).name("Non-existent").version(0L).build();

    when(testerRepository.findAllById(List.of(999L))).thenReturn(List.of());

    assertThatThrownBy(() -> testerService.updateTesters(new ArrayList<>(List.of(dto))))
        .isInstanceOf(EntityBulkUpdateException.class)
        .satisfies(
            exception -> {
              EntityBulkUpdateException e = (EntityBulkUpdateException) exception;
              assertThat(e.getSearchedIds()).containsExactly(999L);
              assertThat(e.getFoundEntities()).isEmpty();
            });

    verify(testerRepository).findAllById(List.of(999L));
    verify(testerRepository, never()).saveAll(anyList());
  }

  @Test
  @DisplayName("updateTesters - Should throw exception when multiple entities not found")
  void updateTesters_WhenMultipleEntitiesNotFound_ShouldThrowException() {
    TesterDto dto1 = TesterDto.builder().id(1L).name("Exists").version(0L).build();

    TesterDto dto2 = TesterDto.builder().id(999L).name("Not Found 1").version(0L).build();

    TesterDto dto3 = TesterDto.builder().id(1000L).name("Not Found 2").version(0L).build();

    Tester entity = new Tester();
    entity.setId(1L);

    when(testerRepository.findAllById(List.of(1L, 999L, 1000L))).thenReturn(List.of(entity));

    assertThatThrownBy(
            () -> testerService.updateTesters(new ArrayList<>(List.of(dto1, dto2, dto3))))
        .isInstanceOf(EntityBulkUpdateException.class)
        .satisfies(
            exception -> {
              EntityBulkUpdateException e = (EntityBulkUpdateException) exception;
              assertThat(e.getSearchedIds()).containsExactlyInAnyOrder(1L, 999L, 1000L);
              assertThat(e.getFoundEntities()).hasSize(1);
            });

    verify(testerRepository).findAllById(List.of(1L, 999L, 1000L));
    verify(testerRepository, never()).saveAll(anyList());
  }

  @Test
  @DisplayName("updateTesters - Should preserve order in results")
  void updateTesters_ShouldPreserveOrderInResults() {
    TesterDto dto1 =
        TesterDto.builder()
            .id(1L)
            .name("First")
            .tags(Set.of(TesterTagType.TAG1))
            .version(0L)
            .build();

    TesterDto dto2 =
        TesterDto.builder()
            .id(null)
            .name("Second")
            .tags(Set.of(TesterTagType.TAG2))
            .version(0L)
            .build();

    TesterDto dto3 =
        TesterDto.builder()
            .id(2L)
            .name("Third")
            .tags(Set.of(TesterTagType.TAG3))
            .version(0L)
            .build();

    Tester entity1 = new Tester();
    entity1.setId(1L);
    entity1.setName("Old First");
    entity1.setTags(new HashSet<>());
    entity1.setVersion(0L);

    Tester entity3 = new Tester();
    entity3.setId(2L);
    entity3.setName("Old Third");
    entity3.setTags(new HashSet<>());
    entity3.setVersion(0L);

    when(testerRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(entity1, entity3));
    when(testerRepository.saveAll(
            argThat(
                list -> {
                  if (list instanceof List<?> l) {
                    return l.size() == 2;
                  }
                  return false;
                })))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(testerRepository.saveAll(
            argThat(
                list -> {
                  if (list instanceof List<?> l) {
                    Tester t = (Tester) l.get(0);
                    return l.size() == 1 && t.getId() == 0L;
                  }
                  return false;
                })))
        .thenAnswer(
            invocation -> {
              List<Tester> entities = invocation.getArgument(0);
              entities.get(0).setId(100L);
              return entities;
            });

    List<TesterDto> result =
        testerService.updateTesters(new ArrayList<>(List.of(dto1, dto2, dto3)));

    assertThat(result).hasSize(3);
    assertThat(result.stream().map(TesterDto::name).toList())
        .containsExactlyInAnyOrder("First", "Second", "Third");
    assertThat(result.stream().map(TesterDto::id).toList()).containsExactlyInAnyOrder(1L, 100L, 2L);
  }

  @Test
  @DisplayName("updateTesters - Should correctly partition entities")
  void updateTesters_ShouldCorrectlyPartitionEntities() {
    final var withId1 =
        TesterDto.builder()
            .id(1L)
            .name("Has ID 1")
            .tags(Set.of(TesterTagType.TAG1))
            .version(0L)
            .build();
    final var withoutId1 =
        TesterDto.builder()
            .id(null)
            .name("No ID 1")
            .tags(Set.of(TesterTagType.TAG2))
            .version(0L)
            .build();
    final var withId2 =
        TesterDto.builder()
            .id(2L)
            .name("Has ID 2")
            .tags(Set.of(TesterTagType.TAG3))
            .version(0L)
            .build();
    final var withoutId2 =
        TesterDto.builder()
            .id(null)
            .name("No ID 2")
            .tags(Set.of(TesterTagType.TAG4))
            .version(0L)
            .build();

    final var inputList = new ArrayList<>(List.of(withId1, withoutId1, withId2, withoutId2));

    final var entity1 = new Tester();
    entity1.setId(1L);
    entity1.setName("Old 1");
    entity1.setTags(new HashSet<>());
    entity1.setVersion(0L);

    final var entity2 = new Tester();
    entity2.setId(2L);
    entity2.setName("Old 2");
    entity2.setTags(new HashSet<>());
    entity2.setVersion(0L);

    when(testerRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(entity1, entity2));
    when(testerRepository.saveAll(anyList()))
        .thenAnswer(
            invocation -> {
              List<Tester> entities = invocation.getArgument(0);
              long id = 100L;
              for (Tester entity : entities) {
                if (entity.getId() == 0L) {
                  entity.setId(id++);
                }
              }
              return entities;
            });

    List<TesterDto> result = testerService.updateTesters(inputList);

    // Verify partition logic
    ArgumentCaptor<List<Long>> idsCaptor = ArgumentCaptor.forClass(List.class);
    verify(testerRepository).findAllById(idsCaptor.capture());
    assertThat(idsCaptor.getValue()).containsExactlyInAnyOrder(1L, 2L);

    // Verify all DTOs are processed
    assertThat(result).hasSize(4);
    assertThat(result.stream().filter(dto -> dto.id() != null && dto.id() <= 2L).count())
        .isEqualTo(2);
    assertThat(result.stream().filter(dto -> dto.id() != null && dto.id() >= 100L).count())
        .isEqualTo(2);
  }
}
