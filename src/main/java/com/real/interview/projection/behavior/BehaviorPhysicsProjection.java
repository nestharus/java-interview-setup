package com.real.interview.projection.behavior;

import com.real.interview.domain.behavior.BehaviorType;

public record BehaviorPhysicsProjection(BehaviorType type, double weight)
    implements AbstractBehaviorProjection {}
