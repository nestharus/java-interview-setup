package com.real.interview.domain.behavior;

import jakarta.persistence.Embeddable;

@Embeddable
public record BehaviorId(long testerId) {}
