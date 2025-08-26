package com.real.interview.exception;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.lang.reflect.Method;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
public class TestGlobalExceptionHandler {
  @Autowired
  private WebApplicationContext webApplicationContext;
  @Autowired
  private InlinedMockController inlinedMockController;
  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
  }

  @Test
  void handleIllegalArgumentException() throws Exception {
    doThrow(new IllegalArgumentException("Test illegal argument exception message"))
        .when(inlinedMockController).triggerException();

    mockMvc
        .perform(get("/mock-controller/trigger-exception").header("X-User-ID", "1"))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("Test illegal argument exception message"));
  }

  @Test
  void handleMethodArgumentNotValidException() throws Exception {
    final var mockMethodParameter = mock(MethodParameter.class);
    final var mockBindingResult = mock(BindingResult.class);
    final var mockMethod = mock(Method.class);

    when(mockBindingResult.getFieldErrors()).thenReturn(List.of());
    when(mockMethodParameter.getExecutable()).thenReturn(mockMethod);
    when(mockMethod.toGenericString()).thenReturn("mockMethod()");

    doThrow(new MethodArgumentNotValidException(mockMethodParameter, mockBindingResult))
        .when(inlinedMockController).triggerException();

    mockMvc.perform(get("/mock-controller/trigger-exception").header("X-User-ID", "1"));
  }

  @Test
  void handleOptimisticLockingFailureException() throws Exception {
    doThrow(new ObjectOptimisticLockingFailureException("Test entity", null))
        .when(inlinedMockController).triggerException();

    mockMvc
        .perform(get("/mock-controller/trigger-exception").header("X-User-ID", "1"))
        .andExpect(status().isConflict())
        .andExpect(
            content()
                .string(
                    "Conflict: The data was modified by another user. Please refresh and try again."));
  }

  @Test
  void handleNoSuchElementException() throws Exception {
    doThrow(new NoSuchElementException("Test no such element exception message"))
        .when(inlinedMockController).triggerException();

    mockMvc
        .perform(get("/mock-controller/trigger-exception").header("X-User-ID", "1"))
        .andExpect(status().isNotFound())
        .andExpect(content().string("The requested resource could not be found."));
  }

  @Test
  void handleDataIntegrityViolationException() throws Exception {
    doThrow(new DataIntegrityViolationException("Test data integrity violation exception message"))
        .when(inlinedMockController).triggerException();

    mockMvc
        .perform(get("/mock-controller/trigger-exception").header("X-User-ID", "1"))
        .andExpect(status().isConflict())
        .andExpect(
            content().string("Data conflict or integrity violation. Please check your input."));
  }

  @Test
  void handleRuntimeException() throws Exception {
    doThrow(new RuntimeException("Test runtime exception message"))
        .when(inlinedMockController).triggerException();

    mockMvc
        .perform(get("/mock-controller/trigger-exception").header("X-User-ID", "1"))
        .andExpect(status().isInternalServerError())
        .andExpect(content().string("Test runtime exception message"));
  }

  @RestController
  @RequestMapping("/mock-controller")
  public interface InlinedMockController {
    @GetMapping("/trigger-exception")
    void triggerException() throws Exception;
  }

  @TestConfiguration
  static class TestConfig {
    @Bean
    public InlinedMockController inlinedMockController() {
      return mock(InlinedMockController.class);
    }
  }
}
