package com.real.interview.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.real.interview.audit.AuditInfo;
import com.real.interview.domain.tester.Tester;
import com.real.interview.domain.tester.TesterTagType;
import com.real.interview.domain.tester2.Tester2;
import com.real.interview.dto.tester.TesterDto;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@DisplayName("TesterMapper Component Tests")
class TestTesterMapper {

  @Autowired private TesterMapper testerMapper;

  private Tester sampleTester;

  @BeforeEach
  void setUp() {
    sampleTester = new Tester();
    sampleTester.setId(1L);
    sampleTester.setName("Test Tester");
    sampleTester.setTags(new HashSet<>(Set.of(TesterTagType.TAG1, TesterTagType.TAG2)));
    sampleTester.setVersion(2L);

    AuditInfo auditInfo = new AuditInfo();
    auditInfo.setCreatedDate(Instant.now());
    auditInfo.setLastModifiedDate(Instant.now());
    auditInfo.setCreatedBy("testuser");
    auditInfo.setLastModifiedBy("anotheruser");
    sampleTester.setAuditInfo(auditInfo);
  }

  @Test
  @DisplayName("toDto - Should map all basic fields")
  void toDto_ShouldMapAllBasicFields() {
    final var dto = testerMapper.toDto(sampleTester);

    assertThat(dto).isNotNull();
    assertThat(dto.id()).isEqualTo(1L);
    assertThat(dto.name()).isEqualTo("Test Tester");
    assertThat(dto.tags()).containsExactlyInAnyOrder(TesterTagType.TAG1, TesterTagType.TAG2);
    assertThat(dto.version()).isEqualTo(2L);
  }

  @Test
  @DisplayName("toDto - Should handle null entity")
  void toDto_WithNullEntity_ShouldReturnNull() {
    final var dto = testerMapper.toDto(null);

    assertThat(dto).isNull();
  }

  @Test
  @DisplayName("toDto - Should handle entity with null tags")
  void toDto_WithNullTags_ShouldMapCorrectly() {
    sampleTester.setTags(null);

    final var dto = testerMapper.toDto(sampleTester);

    assertThat(dto).isNotNull();
    assertThat(dto.tags()).isNull();
  }

  @Test
  @DisplayName("toDto - Should handle entity with empty tags")
  void toDto_WithEmptyTags_ShouldMapCorrectly() {
    sampleTester.setTags(new HashSet<>());

    final var dto = testerMapper.toDto(sampleTester);

    assertThat(dto).isNotNull();
    assertThat(dto.tags()).isEmpty();
  }

  @Test
  @DisplayName("toEntity - Should create new entity from DTO")
  void toEntity_ShouldCreateNewEntity() {
    final var dto =
        TesterDto.builder()
            .id(null)
            .name("New Tester")
            .tags(Set.of(TesterTagType.TAG1))
            .version(0L)
            .build();

    final var entity = testerMapper.toEntity(dto);

    assertThat(entity).isNotNull();
    assertThat(entity.getId()).isEqualTo(0L);
    assertThat(entity.getName()).isEqualTo("New Tester");
    assertThat(entity.getTags()).containsExactly(TesterTagType.TAG1);
    assertThat(entity.getVersion()).isEqualTo(0L);
    assertThat(entity.getTester2List()).isNull();
  }

  @Test
  @DisplayName("toEntity - Should handle null DTO")
  void toEntity_WithNullDto_ShouldReturnNull() {
    final var entity = testerMapper.toEntity(null);

    assertThat(entity).isNull();
  }

  @Test
  @DisplayName("updateEntity - Should update existing entity from DTO")
  void updateEntity_ShouldUpdateExistingEntity() {
    final var existingEntity = new Tester();
    existingEntity.setId(10L);
    existingEntity.setName("Old Name");
    existingEntity.setTags(new HashSet<>(Set.of(TesterTagType.TAG2)));
    existingEntity.setVersion(1L);

    final var tester2List = new ArrayList<Tester2>();
    final var tester2 = new Tester2();
    tester2.setId(100L);
    tester2List.add(tester2);
    existingEntity.setTester2List(tester2List);

    final var updateDto =
        TesterDto.builder()
            .id(10L)
            .name("Updated Name")
            .tags(Set.of(TesterTagType.TAG1, TesterTagType.TAG3))
            .version(2L)
            .build();

    testerMapper.updateEntity(updateDto, existingEntity);

    assertThat(existingEntity.getId()).isEqualTo(10L);
    assertThat(existingEntity.getName()).isEqualTo("Updated Name");
    assertThat(existingEntity.getTags())
        .containsExactlyInAnyOrder(TesterTagType.TAG1, TesterTagType.TAG3);
    assertThat(existingEntity.getVersion()).isEqualTo(2L);
    assertThat(existingEntity.getTester2List()).isSameAs(tester2List);
  }

  @Test
  @DisplayName("updateEntity - Should preserve tester2List during update")
  void updateEntity_ShouldPreserveTester2List() {
    final var existingEntity = new Tester();
    final var originalTester2List = new ArrayList<Tester2>();
    originalTester2List.add(new Tester2());
    existingEntity.setTester2List(originalTester2List);

    final var updateDto =
        TesterDto.builder().id(1L).name("New Name").tags(Set.of()).version(0L).build();

    testerMapper.updateEntity(updateDto, existingEntity);

    assertThat(existingEntity.getTester2List()).isSameAs(originalTester2List);
  }

  @Test
  @DisplayName("updateEntity - Should handle null tags in DTO")
  void updateEntity_WithNullTagsInDto_ShouldUpdateToNull() {
    final var existingEntity = new Tester();
    existingEntity.setTags(new HashSet<>(Set.of(TesterTagType.TAG1)));

    final var updateDto = TesterDto.builder().id(1L).name("Name").tags(null).version(0L).build();

    testerMapper.updateEntity(updateDto, existingEntity);

    assertThat(existingEntity.getTags()).isNull();
  }

  @Test
  @DisplayName("toEntity - Should map from DTO with all fields")
  void toEntity_WithAllFields_ShouldMapCorrectly() {
    final var dto =
        TesterDto.builder()
            .id(50L)
            .name("Full DTO")
            .tags(Set.of(TesterTagType.TAG1, TesterTagType.TAG2, TesterTagType.TAG3))
            .version(5L)
            .build();

    final var entity = testerMapper.toEntity(dto);

    assertThat(entity).isNotNull();
    assertThat(entity.getId()).isEqualTo(50L);
    assertThat(entity.getName()).isEqualTo("Full DTO");
    assertThat(entity.getTags())
        .containsExactlyInAnyOrder(TesterTagType.TAG1, TesterTagType.TAG2, TesterTagType.TAG3);
    assertThat(entity.getVersion()).isEqualTo(5L);
  }

  @Test
  @DisplayName("Round-trip mapping - Should preserve data")
  void roundTripMapping_ShouldPreserveData() {
    final var originalEntity = new Tester();
    originalEntity.setId(123L);
    originalEntity.setName("Round Trip Test");
    originalEntity.setTags(new HashSet<>(Set.of(TesterTagType.TAG1)));
    originalEntity.setVersion(3L);

    final var dto = testerMapper.toDto(originalEntity);
    final var recreatedEntity = testerMapper.toEntity(dto);

    assertThat(recreatedEntity).isNotNull();
    assertThat(recreatedEntity.getId()).isEqualTo(originalEntity.getId());
    assertThat(recreatedEntity.getName()).isEqualTo(originalEntity.getName());
    assertThat(recreatedEntity.getTags()).isEqualTo(originalEntity.getTags());
    assertThat(recreatedEntity.getVersion()).isEqualTo(originalEntity.getVersion());
  }
}
