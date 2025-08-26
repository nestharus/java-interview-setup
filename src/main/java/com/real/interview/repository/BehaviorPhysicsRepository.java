package com.real.interview.repository;

import com.real.interview.domain.behavior.BehaviorId;
import com.real.interview.domain.behavior.BehaviorPhysics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface BehaviorPhysicsRepository extends JpaRepository<BehaviorPhysics, BehaviorId> {}
