package com.real.interview.audit;

import jakarta.persistence.Embeddable;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

@Embeddable
@Getter
@Setter
public class AuditInfo {
  @CreatedDate private Instant createdDate;

  @LastModifiedDate private Instant lastModifiedDate;

  @CreatedBy private long createdBy;

  @LastModifiedBy private long lastModifiedBy;
}
