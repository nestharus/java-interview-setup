package com.real.interview.projection.tester;

import com.real.interview.domain.behavior.BehaviorType;
import com.real.interview.domain.tester.Tester;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public record TesterBehaviorTypesProjection(Tester tester, List<BehaviorType> behaviors) {
  public TesterBehaviorTypesProjection(
      final Tester tester, final BehaviorType behaviorInput, final BehaviorType behaviorPhysics) {
    this(tester, Stream.of(behaviorInput, behaviorPhysics).filter(Objects::nonNull).toList());
  }
}
