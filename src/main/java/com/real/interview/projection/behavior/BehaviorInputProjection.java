package com.real.interview.projection.behavior;

import com.real.interview.domain.behavior.BehaviorType;

public record BehaviorInputProjection(BehaviorType type, double velocity)
    implements AbstractBehaviorProjection {}
