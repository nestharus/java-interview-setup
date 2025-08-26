package com.real.interview.domain.behavior;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class BehaviorInput extends AbstractBehavior {
  @Column(nullable = false)
  private double velocity;
}
