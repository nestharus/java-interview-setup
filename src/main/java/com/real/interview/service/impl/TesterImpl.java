package com.real.interview.service.impl;

import com.real.interview.dto.Dto;
import com.real.interview.service.Tester;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

@Service
public class TesterImpl implements Tester {
  private static final Logger logger = org.slf4j.LoggerFactory.getLogger(TesterImpl.class);

  @PostConstruct
  public void init() {
    final var dto = Dto.builder().myInt(5).build();
    logger.info(String.valueOf(dto.getMyInt()));
  }

  public void test() {
    final var dto = Dto.builder().myInt(5).build();
    logger.info(String.valueOf(dto.getMyInt()));
  }
}
