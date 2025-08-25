package com.real.interview;

import com.real.interview.service.Tester;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class JavaBackendInterviewApplicationTests {
  private static final Logger logger = LoggerFactory.getLogger(JavaBackendInterviewApplicationTests.class);

  @Autowired
  private Tester tester;

  @Test
	void contextLoads() {
    logger.info("hello");

    tester.test();
	}

}
