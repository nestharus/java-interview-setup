package com.real.interview.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.real.interview.domain.behavior.BehaviorId;
import com.real.interview.domain.behavior.BehaviorInput;
import com.real.interview.domain.behavior.BehaviorPhysics;
import com.real.interview.domain.behavior.BehaviorType;
import com.real.interview.domain.tester.Tester;
import com.real.interview.projection.behavior.BehaviorInputProjection;
import com.real.interview.projection.behavior.BehaviorPhysicsProjection;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("TesterRepository Custom Query Tests")
class TestTesterRepository {

  @Autowired private TesterRepository testerRepository;

  @Autowired private BehaviorInputRepository behaviorInputRepository;

  @Autowired private BehaviorPhysicsRepository behaviorPhysicsRepository;

  @Autowired private EntityManager entityManager;

  private Tester savedTester;

  @BeforeEach
  void setUp() {
    final var tester = new Tester();
    tester.setName("Query Test Tester");
    savedTester = testerRepository.save(tester);
    entityManager.flush();
    entityManager.clear();
  }

  @Test
  @DisplayName("Should get behavior types when only BehaviorInput exists")
  void shouldGetBehaviorTypesWithOnlyInput() {
    final var behaviorInput = new BehaviorInput();
    behaviorInput.setId(new BehaviorId(savedTester.getId()));
    behaviorInput.setVelocity(100.5);
    behaviorInputRepository.save(behaviorInput);
    entityManager.flush();
    entityManager.clear();

    final var result = testerRepository.getBehaviorTypes(savedTester.getId());

    assertThat(result).isPresent();
    assertThat(result.get().tester().getId()).isEqualTo(savedTester.getId());
    assertThat(result.get().behaviors()).containsExactly(BehaviorType.INPUT);
  }

  @Test
  @DisplayName("Should get behavior types when only BehaviorPhysics exists")
  void shouldGetBehaviorTypesWithOnlyPhysics() {
    final var behaviorPhysics = new BehaviorPhysics();
    behaviorPhysics.setId(new BehaviorId(savedTester.getId()));
    behaviorPhysics.setWeight(50.25);
    behaviorPhysicsRepository.save(behaviorPhysics);
    entityManager.flush();
    entityManager.clear();

    final var result = testerRepository.getBehaviorTypes(savedTester.getId());

    assertThat(result).isPresent();
    assertThat(result.get().tester().getId()).isEqualTo(savedTester.getId());
    assertThat(result.get().behaviors()).containsExactly(BehaviorType.PHYSICS);
  }

  @Test
  @DisplayName("Should get behavior types when both behaviors exist")
  void shouldGetBehaviorTypesWithBothBehaviors() {
    final var behaviorInput = new BehaviorInput();
    behaviorInput.setId(new BehaviorId(savedTester.getId()));
    behaviorInput.setVelocity(100.5);
    behaviorInputRepository.save(behaviorInput);

    final var behaviorPhysics = new BehaviorPhysics();
    behaviorPhysics.setId(new BehaviorId(savedTester.getId()));
    behaviorPhysics.setWeight(50.25);
    behaviorPhysicsRepository.save(behaviorPhysics);

    entityManager.flush();
    entityManager.clear();

    final var result = testerRepository.getBehaviorTypes(savedTester.getId());

    assertThat(result).isPresent();
    assertThat(result.get().tester().getId()).isEqualTo(savedTester.getId());
    assertThat(result.get().behaviors())
        .containsExactlyInAnyOrder(BehaviorType.INPUT, BehaviorType.PHYSICS);
  }

  @Test
  @DisplayName("Should get empty behavior types when no behaviors exist")
  void shouldGetEmptyBehaviorTypesWhenNoBehaviors() {
    final var result = testerRepository.getBehaviorTypes(savedTester.getId());

    assertThat(result).isPresent();
    assertThat(result.get().tester().getId()).isEqualTo(savedTester.getId());
    assertThat(result.get().behaviors()).isEmpty();
  }

  @Test
  @DisplayName("Should return empty Optional for getBehaviorTypes when Tester not found")
  void shouldReturnEmptyForGetBehaviorTypesWhenTesterNotFound() {
    final var result = testerRepository.getBehaviorTypes(999L);

    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("Should get behaviors with BehaviorInput details")
  void shouldGetBehaviorsWithInputDetails() {
    final var behaviorInput = new BehaviorInput();
    behaviorInput.setId(new BehaviorId(savedTester.getId()));
    behaviorInput.setVelocity(100.5);
    behaviorInputRepository.save(behaviorInput);
    entityManager.flush();
    entityManager.clear();

    final var result = testerRepository.getBehaviors(savedTester.getId());

    assertThat(result).isPresent();
    assertThat(result.get().tester().getId()).isEqualTo(savedTester.getId());
    assertThat(result.get().behaviors()).hasSize(1);
    assertThat(result.get().behaviors().getFirst()).isInstanceOf(BehaviorInputProjection.class);

    final var inputProjection = (BehaviorInputProjection) result.get().behaviors().getFirst();
    assertThat(inputProjection.type()).isEqualTo(BehaviorType.INPUT);
    assertThat(inputProjection.velocity()).isEqualTo(100.5);
  }

  @Test
  @DisplayName("Should get behaviors with BehaviorPhysics details")
  void shouldGetBehaviorsWithPhysicsDetails() {
    final var behaviorPhysics = new BehaviorPhysics();
    behaviorPhysics.setId(new BehaviorId(savedTester.getId()));
    behaviorPhysics.setWeight(50.25);
    behaviorPhysicsRepository.save(behaviorPhysics);
    entityManager.flush();
    entityManager.clear();

    final var result = testerRepository.getBehaviors(savedTester.getId());

    assertThat(result).isPresent();
    assertThat(result.get().tester().getId()).isEqualTo(savedTester.getId());
    assertThat(result.get().behaviors()).hasSize(1);
    assertThat(result.get().behaviors().getFirst()).isInstanceOf(BehaviorPhysicsProjection.class);

    final var physicsProjection = (BehaviorPhysicsProjection) result.get().behaviors().getFirst();
    assertThat(physicsProjection.type()).isEqualTo(BehaviorType.PHYSICS);
    assertThat(physicsProjection.weight()).isEqualTo(50.25);
  }

  @Test
  @DisplayName("Should get behaviors with both behavior details")
  void shouldGetBehaviorsWithBothBehaviorDetails() {
    final var behaviorInput = new BehaviorInput();
    behaviorInput.setId(new BehaviorId(savedTester.getId()));
    behaviorInput.setVelocity(100.5);
    behaviorInputRepository.save(behaviorInput);

    final var behaviorPhysics = new BehaviorPhysics();
    behaviorPhysics.setId(new BehaviorId(savedTester.getId()));
    behaviorPhysics.setWeight(50.25);
    behaviorPhysicsRepository.save(behaviorPhysics);

    entityManager.flush();
    entityManager.clear();

    final var result = testerRepository.getBehaviors(savedTester.getId());

    assertThat(result).isPresent();
    assertThat(result.get().tester().getId()).isEqualTo(savedTester.getId());
    assertThat(result.get().behaviors()).hasSize(2);

    final var inputProjection =
        result.get().behaviors().stream()
            .filter(BehaviorInputProjection.class::isInstance)
            .map(BehaviorInputProjection.class::cast)
            .findFirst()
            .orElseThrow();
    assertThat(inputProjection.type()).isEqualTo(BehaviorType.INPUT);
    assertThat(inputProjection.velocity()).isEqualTo(100.5);

    final var physicsProjection =
        result.get().behaviors().stream()
            .filter(BehaviorPhysicsProjection.class::isInstance)
            .map(BehaviorPhysicsProjection.class::cast)
            .findFirst()
            .orElseThrow();
    assertThat(physicsProjection.type()).isEqualTo(BehaviorType.PHYSICS);
    assertThat(physicsProjection.weight()).isEqualTo(50.25);
  }

  @Test
  @DisplayName("Should get empty behaviors when no behaviors exist")
  void shouldGetEmptyBehaviorsWhenNoBehaviors() {
    final var result = testerRepository.getBehaviors(savedTester.getId());

    assertThat(result).isPresent();
    assertThat(result.get().tester().getId()).isEqualTo(savedTester.getId());
    assertThat(result.get().behaviors()).isEmpty();
  }

  @Test
  @DisplayName("Should return empty Optional for getBehaviors when Tester not found")
  void shouldReturnEmptyForGetBehaviorsWhenTesterNotFound() {
    final var result = testerRepository.getBehaviors(999L);

    assertThat(result).isEmpty();
  }
}
