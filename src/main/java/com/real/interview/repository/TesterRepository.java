package com.real.interview.repository;

import com.real.interview.domain.tester.Tester;
import com.real.interview.projection.tester.TesterBehaviorTypesProjection;
import com.real.interview.projection.tester.TesterBehaviorsProjection;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface TesterRepository extends JpaRepository<Tester, Long> {
  @Query(
      """
    SELECT NEW com.real.interview.projection.tester.TesterBehaviorTypesProjection(tester,
      CASE
        WHEN (input IS NOT NULL)
        THEN 'INPUT'
        ELSE NULL
      END,
      CASE
        WHEN (physics IS NOT NULL)
        THEN 'PHYSICS'
        ELSE NULL
      END
    )
    FROM Tester tester
    LEFT JOIN BehaviorInput input ON tester.id = input.id.testerId
    LEFT JOIN BehaviorPhysics physics ON tester.id = physics.id.testerId
    WHERE tester.id = :id
  """)
  Optional<TesterBehaviorTypesProjection> getBehaviorTypes(@Param("id") long id);

  @Query(
      """
    SELECT NEW com.real.interview.projection.tester.TesterBehaviorsProjection(tester,
      CASE
        WHEN (input IS NOT NULL)
        THEN 'INPUT'
        ELSE NULL
      END,
      input.velocity,
      CASE
        WHEN (physics IS NOT NULL)
        THEN 'PHYSICS'
        ELSE NULL
      END,
      physics.weight
    )
    FROM Tester tester
    LEFT JOIN BehaviorInput input ON tester.id = input.id.testerId
    LEFT JOIN BehaviorPhysics physics ON tester.id = physics.id.testerId
    WHERE tester.id = :id
  """)
  Optional<TesterBehaviorsProjection> getBehaviors(@Param("id") long id);
}
