package com.real.interview.projection.tester;

import com.real.interview.domain.behavior.BehaviorType;
import com.real.interview.domain.tester.Tester;
import com.real.interview.projection.behavior.AbstractBehaviorProjection;
import com.real.interview.projection.behavior.BehaviorInputProjection;
import com.real.interview.projection.behavior.BehaviorPhysicsProjection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public record TesterBehaviorsProjection(Tester tester, List<AbstractBehaviorProjection> behaviors) {
  public TesterBehaviorsProjection(
      final Tester tester,
      final String behaviorInputType,
      final Double inputVelocity,
      final String behaviorPhysicsType,
      final Double physicsWeight) {
    this(
        tester,
        Stream.of(
                behaviorInputType != null
                    ? new BehaviorInputProjection(
                        BehaviorType.valueOf(behaviorInputType), inputVelocity)
                    : null,
                behaviorPhysicsType != null
                    ? new BehaviorPhysicsProjection(
                        BehaviorType.valueOf(behaviorPhysicsType), physicsWeight)
                    : null)
            .filter(Objects::nonNull)
            .map(AbstractBehaviorProjection.class::cast)
            .toList());
  }
}
