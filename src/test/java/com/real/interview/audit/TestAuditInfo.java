package com.real.interview.audit;

import static com.real.interview.common.validator.EntityValidator.validateIsNotStale;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.real.interview.common.entity.AbstractEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.NoSuchElementException;
import java.util.stream.IntStream;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("AuditInfo Component Tests")
@Import(TestAuditInfo.TestConfig.class)
public class TestAuditInfo {

  private static final String USERNAME = "testuser";
  private static final String ANOTHER_USERNAME = "anotheruser";
  @Autowired private WebApplicationContext webApplicationContext;
  @Autowired private InlinedAuditTestController inlinedAuditTestController;
  @Autowired private TestAuditEntityRepository testAuditEntityRepository;
  @Autowired private EntityManager entityManager;
  private MockMvc mockMvc;

  @BeforeEach
  @Transactional
  void setUp() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    testAuditEntityRepository.deleteAll();
  }

  @Test
  @Transactional
  @DisplayName("Should populate audit fields on entity creation with header")
  void shouldPopulateAuditFieldsOnCreation() throws Exception {
    final var beforeCreate = Instant.now();

    mockMvc
        .perform(
            post("/audit-test/create").param("name", "Test Entity").header("X-Username", USERNAME))
        .andExpect(status().isOk());

    // Check the created entity's audit fields
    entityManager.flush();
    entityManager.clear();

    final var savedEntity =
        testAuditEntityRepository.findAll().stream()
            .filter(e -> "Test Entity".equals(e.getName()))
            .findFirst()
            .orElseThrow();

    final var auditInfo = savedEntity.getAuditInfo();
    assertThat(auditInfo).isNotNull();
    assertThat(auditInfo.getCreatedBy()).isEqualTo(USERNAME);
    assertThat(auditInfo.getLastModifiedBy()).isEqualTo(USERNAME);
    assertThat(auditInfo.getCreatedDate()).isNotNull();
    assertThat(auditInfo.getCreatedDate()).isCloseTo(beforeCreate, within(1, ChronoUnit.SECONDS));
    assertThat(auditInfo.getLastModifiedDate()).isNotNull();
    assertThat(auditInfo.getLastModifiedDate())
        .isCloseTo(beforeCreate, within(1, ChronoUnit.SECONDS));
    assertThat(auditInfo.getCreatedDate()).isEqualTo(auditInfo.getLastModifiedDate());
  }

  @Test
  @Transactional
  @DisplayName("Should update audit fields on entity modification with different user")
  void shouldUpdateAuditFieldsOnModification() throws Exception {
    mockMvc
        .perform(
            post("/audit-test/create")
                .param("name", "Original Entity")
                .header("X-Username", USERNAME))
        .andExpect(status().isOk());

    entityManager.flush();
    entityManager.clear();

    final var createdEntity =
        testAuditEntityRepository.findAll().stream()
            .filter(e -> "Original Entity".equals(e.getName()))
            .findFirst()
            .orElseThrow();

    final var originalAudit = createdEntity.getAuditInfo();
    final var originalCreatedDate = originalAudit.getCreatedDate();
    final var originalModifiedDate = originalAudit.getLastModifiedDate();
    final var originalCreatedBy = originalAudit.getCreatedBy();

    Thread.sleep(10); // Ensure time difference

    // Now update with ANOTHER_USERNAME
    final var entityId = createdEntity.getId();
    final var version = createdEntity.getVersion();
    mockMvc
        .perform(
            put("/audit-test/update/" + entityId)
                .param("name", "Updated Entity")
                .param("version", String.valueOf(version))
                .header("X-Username", ANOTHER_USERNAME))
        .andExpect(status().isOk());

    entityManager.flush();
    entityManager.clear();

    final var updatedEntity = testAuditEntityRepository.findById(entityId).orElseThrow();
    final var updatedAudit = updatedEntity.getAuditInfo();

    assertThat(updatedAudit.getCreatedBy()).isEqualTo(originalCreatedBy);
    assertThat(updatedAudit.getCreatedDate()).isEqualTo(originalCreatedDate);
    assertThat(updatedAudit.getLastModifiedBy()).isEqualTo(ANOTHER_USERNAME);
    assertThat(updatedAudit.getLastModifiedDate()).isAfter(originalModifiedDate);
  }

  @Test
  @DisplayName("Should handle optimistic locking conflict")
  void shouldHandleOptimisticLockingConflict() throws Exception {
    mockMvc
        .perform(
            post("/audit-test/create").param("name", "Locking Test").header("X-Username", USERNAME))
        .andExpect(status().isOk());

    final var entity =
        testAuditEntityRepository.findAll().stream()
            .filter(e -> "Locking Test".equals(e.getName()))
            .findFirst()
            .orElseThrow();

    final var entityId = entity.getId();
    final var originalVersion = entity.getVersion();

    // First update - this will increment the version
    mockMvc
        .perform(
            put("/audit-test/update/" + entityId)
                .param("name", "First Update")
                .param("version", String.valueOf(originalVersion))
                .header("X-Username", USERNAME))
        .andExpect(status().isOk());

    // Second update with stale version - should fail due to optimistic locking
    // The real controller will naturally throw an OptimisticLockException
    mockMvc
        .perform(
            put("/audit-test/update/" + entityId)
                .param("name", "Stale Update")
                .param("version", String.valueOf(originalVersion)) // Using old version
                .header("X-Username", ANOTHER_USERNAME))
        .andExpect(status().isConflict()); // Optimistic locking failure should return 409 Conflict
  }

  @Test
  @Transactional
  @DisplayName("Should use null for anonymous users when no header present")
  void shouldUseNullForAnonymousUsers() throws Exception {
    // Call without header - should result in null audit fields for anonymous users
    mockMvc
        .perform(post("/audit-test/create").param("name", "Anonymous Test"))
        .andExpect(status().isOk());

    entityManager.flush();
    entityManager.clear();

    final var savedEntity =
        testAuditEntityRepository.findAll().stream()
            .filter(e -> "Anonymous Test".equals(e.getName()))
            .findFirst()
            .orElseThrow();

    final var auditInfo = savedEntity.getAuditInfo();
    assertThat(auditInfo.getCreatedBy()).isNull(); // Anonymous user
    assertThat(auditInfo.getLastModifiedBy()).isNull(); // Anonymous user
    assertThat(auditInfo.getCreatedDate()).isNotNull();
    assertThat(auditInfo.getLastModifiedDate()).isNotNull();
  }

  @Test
  @Transactional
  @DisplayName("Should use username from header")
  void shouldUseUsernameFromHeader() throws Exception {
    // Call with header - should use header value
    mockMvc
        .perform(
            post("/audit-test/create").param("name", "Header Test").header("X-Username", USERNAME))
        .andExpect(status().isOk());

    entityManager.flush();
    entityManager.clear();

    final var savedEntity =
        testAuditEntityRepository.findAll().stream()
            .filter(e -> "Header Test".equals(e.getName()))
            .findFirst()
            .orElseThrow();

    final var auditInfo = savedEntity.getAuditInfo();
    assertThat(auditInfo.getCreatedBy()).isEqualTo(USERNAME);
    assertThat(auditInfo.getLastModifiedBy()).isEqualTo(USERNAME);
  }

  @Test
  @Transactional
  @DisplayName("Should use service name for service-to-service calls")
  void shouldUseServiceNameForInternalCalls() throws Exception {
    String serviceName = "notification-service";

    // Service-to-service call with service name in header
    mockMvc
        .perform(
            post("/audit-test/create")
                .param("name", "Service Call Test")
                .header("X-Username", serviceName))
        .andExpect(status().isOk());

    entityManager.flush();
    entityManager.clear();

    final var savedEntity =
        testAuditEntityRepository.findAll().stream()
            .filter(e -> "Service Call Test".equals(e.getName()))
            .findFirst()
            .orElseThrow();

    final var auditInfo = savedEntity.getAuditInfo();
    assertThat(auditInfo.getCreatedBy()).isEqualTo(serviceName);
    assertThat(auditInfo.getLastModifiedBy()).isEqualTo(serviceName);
  }

  @Test
  @Transactional
  @DisplayName("Should handle empty header value as anonymous user")
  void shouldHandleEmptyHeaderValue() throws Exception {
    // Call with empty header - should result in null audit fields (anonymous user)
    mockMvc
        .perform(
            post("/audit-test/create").param("name", "Empty Header Test").header("X-Username", ""))
        .andExpect(status().isOk());

    entityManager.flush();
    entityManager.clear();

    final var savedEntity =
        testAuditEntityRepository.findAll().stream()
            .filter(e -> "Empty Header Test".equals(e.getName()))
            .findFirst()
            .orElseThrow();

    final var auditInfo = savedEntity.getAuditInfo();
    assertThat(auditInfo.getCreatedBy()).isNull(); // Anonymous user
    assertThat(auditInfo.getLastModifiedBy()).isNull(); // Anonymous user
  }

  @Test
  @Transactional
  @DisplayName("Should preserve created fields across multiple updates")
  void shouldPreserveCreatedFieldsAcrossUpdates() throws Exception {
    mockMvc
        .perform(
            post("/audit-test/create")
                .param("name", "Preserve Test")
                .header("X-Username", USERNAME))
        .andExpect(status().isOk());

    entityManager.flush();
    entityManager.clear();

    final var original =
        testAuditEntityRepository.findAll().stream()
            .filter(e -> "Preserve Test".equals(e.getName()))
            .findFirst()
            .orElseThrow();

    final var entityId = original.getId();
    final var originalCreatedDate = original.getAuditInfo().getCreatedDate();
    final var originalCreatedBy = original.getAuditInfo().getCreatedBy();

    IntStream.range(1, 4)
        .forEach(
            i -> {
              try {
                Thread.sleep(10);
              } catch (final InterruptedException exception) {
                throw new RuntimeException(exception);
              }

              final var currentEntity = testAuditEntityRepository.findById(entityId).orElseThrow();
              final var currentVersion = currentEntity.getVersion();
              final var updateName = "Update " + i;
              final var updateUsername = "user" + i;

              try {
                mockMvc
                    .perform(
                        put("/audit-test/update/" + entityId)
                            .param("name", updateName)
                            .param("version", String.valueOf(currentVersion))
                            .header("X-Username", updateUsername))
                    .andExpect(status().isOk());
              } catch (final Exception exception) {
                throw new RuntimeException(exception);
              }

              entityManager.flush();
              entityManager.clear();

              final var updated = testAuditEntityRepository.findById(entityId).orElseThrow();

              // Created fields should never change
              assertThat(updated.getAuditInfo().getCreatedDate()).isEqualTo(originalCreatedDate);
              assertThat(updated.getAuditInfo().getCreatedBy()).isEqualTo(originalCreatedBy);
              // Modified fields should update
              assertThat(updated.getAuditInfo().getLastModifiedBy()).isEqualTo(updateUsername);
              assertThat(updated.getAuditInfo().getLastModifiedDate()).isAfter(originalCreatedDate);
            });
  }

  public interface TestAuditEntityRepository extends JpaRepository<TestAuditEntity, Long> {}

  // Controller interface
  @RequestMapping("/audit-test")
  public interface InlinedAuditTestController {
    @PostMapping("/create")
    TestAuditInfo.TestAuditEntity createEntity(@RequestParam String name);

    @PutMapping("/update/{id}")
    void updateEntity(@PathVariable Long id, @RequestParam String name, @RequestParam Long version);
  }

  @Entity
  @Getter
  @Setter
  public static class TestAuditEntity extends AbstractEntity {
    private String name;
  }

  @TestConfiguration
  @EntityScan(basePackageClasses = TestAuditInfo.class)
  static class TestConfig {
    @Bean
    public InlinedAuditTestController inlinedAuditTestController(
        TestAuditEntityRepository repository) {
      return new InlinedAuditTestControllerImpl(repository);
    }

    @Bean
    public TestAuditEntityRepository testAuditEntityRepository(EntityManager entityManager) {
      JpaRepositoryFactory factory = new JpaRepositoryFactory(entityManager);
      return factory.getRepository(TestAuditEntityRepository.class);
    }
  }

  @RestController
  public static class InlinedAuditTestControllerImpl implements InlinedAuditTestController {
    private final TestAuditEntityRepository repository;
    @Autowired private EntityManager entityManager;

    public InlinedAuditTestControllerImpl(TestAuditEntityRepository repository) {
      this.repository = repository;
    }

    @Override
    @Transactional
    public TestAuditInfo.TestAuditEntity createEntity(String name) {
      TestAuditEntity entity = new TestAuditEntity();
      entity.setName(name);
      return repository.save(entity);
    }

    @Override
    @Transactional
    public void updateEntity(final Long id, final String name, final Long version) {
      final var entity = repository.findById(id);

      entity
          .map(
              obj -> {
                validateIsNotStale(obj, version);

                obj.setName(name);

                return repository.save(obj);
              })
          .orElseThrow(
              () ->
                  new NoSuchElementException(
                      "Could not find TestAuditEntity %s".formatted(String.valueOf(id))));
    }
  }
}
