package com.real.interview;

import com.real.interview.service.TesterService;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TestApplication {
  private static final Logger logger = LoggerFactory.getLogger(TestApplication.class);

  @Autowired private TesterService testerService;

  @Test
  void contextLoads() {
    logger.info("hello");

    // testerService.getTest();
  }
}
