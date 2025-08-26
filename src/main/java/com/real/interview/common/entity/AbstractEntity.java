package com.real.interview.common.entity;

import com.real.interview.audit.AuditInfo;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public abstract class AbstractEntity implements VersionedEntity<Long> {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE)
  private Long id;

  @Version private long version;

  @Embedded private AuditInfo auditInfo = new AuditInfo();
}
