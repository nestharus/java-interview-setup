package com.real.interview.repository;

import com.real.interview.domain.behavior.BehaviorId;
import com.real.interview.domain.behavior.BehaviorInput;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface BehaviorInputRepository extends JpaRepository<BehaviorInput, BehaviorId> {}
