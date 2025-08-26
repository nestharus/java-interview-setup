package com.real.interview.common.entity;

import com.real.interview.audit.AuditInfo;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
public abstract class AbstractEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE)
  private long id;

  @Version private long version;

  @Embedded private AuditInfo auditInfo = new AuditInfo();
}
