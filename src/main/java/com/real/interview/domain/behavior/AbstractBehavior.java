package com.real.interview.domain.behavior;

import com.real.interview.audit.AuditInfo;
import com.real.interview.common.entity.VersionedEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
public abstract class AbstractBehavior implements VersionedEntity<BehaviorId> {
  @EmbeddedId private BehaviorId id;

  @Version private long version;

  @Embedded private AuditInfo auditInfo = new AuditInfo();
}
