package com.real.interview;

import com.real.interview.service.TesterService;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ApplicationTests {
  private static final Logger logger = LoggerFactory.getLogger(ApplicationTests.class);

  @Autowired private TesterService testerService;

  @Test
  void contextLoads() {
    logger.info("hello");

    // testerService.getTest();
  }
}
