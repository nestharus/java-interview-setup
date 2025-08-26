package com.real.interview.domain.tester;

import com.real.interview.common.entity.AbstractEntity;
import com.real.interview.domain.tester2.Tester2;
import jakarta.persistence.*;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Tester extends AbstractEntity {

  @ElementCollection
  @CollectionTable
  @Enumerated(EnumType.STRING)
  private Set<TesterTagType> tags;

  @OneToMany(
      mappedBy = "tester",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  private List<Tester2> tester2List;

  private String name;
}
