package com.real.interview.domain.tester2;

import com.real.interview.common.entity.AbstractEntity;
import com.real.interview.domain.tester.Tester;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Tester2 extends AbstractEntity {
  @ManyToOne(fetch = FetchType.LAZY)
  private Tester tester;
}
