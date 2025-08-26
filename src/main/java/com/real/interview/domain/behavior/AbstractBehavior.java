package com.real.interview.domain.behavior;

import com.real.interview.audit.AuditInfo;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
public abstract class AbstractBehavior {
  @EmbeddedId private BehaviorId id;

  @Version private long version;

  @Embedded private AuditInfo auditInfo = new AuditInfo();
}
