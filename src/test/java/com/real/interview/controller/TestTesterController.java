package com.real.interview.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.real.interview.domain.tester.Tester;
import com.real.interview.domain.tester.TesterTagType;
import com.real.interview.dto.tester.TesterDto;
import com.real.interview.repository.TesterRepository;
import jakarta.persistence.EntityManager;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("TesterController Integration Tests")
class TestTesterController {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private TesterRepository testerRepository;

  @Autowired private EntityManager entityManager;

  @BeforeEach
  void setUp() {
    testerRepository.deleteAll();
    entityManager.flush();
    entityManager.clear();
  }

  @Test
  @DisplayName("GET /tester/{id} - Should return tester when found")
  void getTest_WhenTesterExists_ShouldReturnTester() throws Exception {
    // Given - create a tester in the database
    Tester tester = new Tester();
    tester.setName("Test Tester");
    tester.setTags(new HashSet<>(Set.of(TesterTagType.TAG1)));
    Tester savedTester = testerRepository.save(tester);
    entityManager.flush();
    entityManager.clear();

    // When & Then
    mockMvc
        .perform(get("/tester/" + savedTester.getId()).header("X-User-ID", "123"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(savedTester.getId()))
        .andExpect(jsonPath("$.name").value("Test Tester"))
        .andExpect(jsonPath("$.tags[0]").value("TAG1"))
        .andExpect(jsonPath("$.version").value(0));
  }

  @Test
  @DisplayName("GET /tester/{id} - Should return empty when not found")
  void getTest_WhenTesterNotFound_ShouldReturnEmpty() throws Exception {
    mockMvc
        .perform(get("/tester/999").header("X-User-ID", "123"))
        .andExpect(status().isOk())
        .andExpect(content().string(""));
  }

  @Test
  @DisplayName("GET /tester/{id} - Should validate ID minimum constraint")
  void getTest_WhenIdLessThanOne_ShouldReturnBadRequest() throws Exception {
    mockMvc.perform(get("/tester/0").header("X-User-ID", "123")).andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("GET /tester/{id} - Should handle invalid ID format")
  void getTest_WhenIdIsInvalid_ShouldReturnBadRequest() throws Exception {
    mockMvc
        .perform(get("/tester/abc").header("X-User-ID", "123"))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("POST /tester/all - Should create new testers")
  void updateTesters_WithNewTesters_ShouldCreateAndReturn() throws Exception {
    // Given
    TesterDto newTester1 =
        TesterDto.builder()
            .id(null)
            .name("New Tester 1")
            .tags(Set.of(TesterTagType.TAG1))
            .version(0L)
            .build();

    TesterDto newTester2 =
        TesterDto.builder()
            .id(null)
            .name("New Tester 2")
            .tags(Set.of(TesterTagType.TAG2))
            .version(0L)
            .build();

    List<TesterDto> inputList = List.of(newTester1, newTester2);

    // When & Then
    String response =
        mockMvc
            .perform(
                post("/tester/all")
                    .header("X-User-ID", "123")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(inputList)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].name").value("New Tester 1"))
            .andExpect(jsonPath("$[0].tags[0]").value("TAG1"))
            .andExpect(jsonPath("$[1].name").value("New Tester 2"))
            .andExpect(jsonPath("$[1].tags[0]").value("TAG2"))
            .andReturn()
            .getResponse()
            .getContentAsString();

    // Verify persistence
    List<Tester> allTesters = testerRepository.findAll();
    assertThat(allTesters).hasSize(2);
    assertThat(allTesters)
        .extracting(Tester::getName)
        .containsExactlyInAnyOrder("New Tester 1", "New Tester 2");
  }

  @Test
  @DisplayName("POST /tester/all - Should update existing testers")
  void updateTesters_WithExistingTesters_ShouldUpdateAndReturn() throws Exception {
    // Given - create existing testers
    Tester existingTester1 = new Tester();
    existingTester1.setName("Original Name 1");
    existingTester1.setTags(new HashSet<>(Set.of(TesterTagType.TAG1)));
    existingTester1 = testerRepository.save(existingTester1);

    Tester existingTester2 = new Tester();
    existingTester2.setName("Original Name 2");
    existingTester2.setTags(new HashSet<>(Set.of(TesterTagType.TAG2)));
    existingTester2 = testerRepository.save(existingTester2);

    entityManager.flush();
    entityManager.clear();

    // Prepare updates
    TesterDto updateDto1 =
        TesterDto.builder()
            .id(existingTester1.getId())
            .name("Updated Name 1")
            .tags(Set.of(TesterTagType.TAG3))
            .version(existingTester1.getVersion())
            .build();

    TesterDto updateDto2 =
        TesterDto.builder()
            .id(existingTester2.getId())
            .name("Updated Name 2")
            .tags(Set.of(TesterTagType.TAG1, TesterTagType.TAG2))
            .version(existingTester2.getVersion())
            .build();

    List<TesterDto> updateList = List.of(updateDto1, updateDto2);

    // When & Then
    mockMvc
        .perform(
            post("/tester/all")
                .header("X-User-ID", "123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateList)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].id").value(existingTester1.getId()))
        .andExpect(jsonPath("$[0].name").value("Updated Name 1"))
        .andExpect(jsonPath("$[0].tags[0]").value("TAG3"))
        .andExpect(jsonPath("$[0].version").value(1))
        .andExpect(jsonPath("$[1].id").value(existingTester2.getId()))
        .andExpect(jsonPath("$[1].name").value("Updated Name 2"))
        .andExpect(jsonPath("$[1].version").value(1));

    // Verify persistence
    entityManager.flush();
    entityManager.clear();

    Tester updatedTester1 = testerRepository.findById(existingTester1.getId()).orElseThrow();
    assertThat(updatedTester1.getName()).isEqualTo("Updated Name 1");
    assertThat(updatedTester1.getTags()).containsExactly(TesterTagType.TAG3);

    Tester updatedTester2 = testerRepository.findById(existingTester2.getId()).orElseThrow();
    assertThat(updatedTester2.getName()).isEqualTo("Updated Name 2");
    assertThat(updatedTester2.getTags())
        .containsExactlyInAnyOrder(TesterTagType.TAG1, TesterTagType.TAG2);
  }

  @Test
  @DisplayName("POST /tester/all - Should handle mixed create and update")
  void updateTesters_WithMixedCreateAndUpdate_ShouldProcessCorrectly() throws Exception {
    // Given - create one existing tester
    Tester existingTester = new Tester();
    existingTester.setName("Existing Tester");
    existingTester.setTags(new HashSet<>(Set.of(TesterTagType.TAG2)));
    existingTester = testerRepository.save(existingTester);
    entityManager.flush();
    entityManager.clear();

    // Prepare mixed list
    TesterDto updateDto =
        TesterDto.builder()
            .id(existingTester.getId())
            .name("Updated Existing")
            .tags(Set.of(TesterTagType.TAG1))
            .version(existingTester.getVersion())
            .build();

    TesterDto newDto =
        TesterDto.builder()
            .id(null)
            .name("Brand New Tester")
            .tags(Set.of(TesterTagType.TAG3))
            .version(0L)
            .build();

    List<TesterDto> mixedList = List.of(updateDto, newDto);

    // When & Then
    mockMvc
        .perform(
            post("/tester/all")
                .header("X-User-ID", "123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mixedList)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[?(@.name == 'Updated Existing')].id").value(existingTester.getId()))
        .andExpect(jsonPath("$[?(@.name == 'Brand New Tester')].id").exists());

    // Verify persistence
    List<Tester> allTesters = testerRepository.findAll();
    assertThat(allTesters).hasSize(2);
    assertThat(allTesters)
        .extracting(Tester::getName)
        .containsExactlyInAnyOrder("Updated Existing", "Brand New Tester");
  }

  @Test
  @DisplayName("POST /tester/all - Should handle empty list")
  void updateTesters_WithEmptyList_ShouldReturnEmptyList() throws Exception {
    mockMvc
        .perform(
            post("/tester/all")
                .header("X-User-ID", "123")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[]"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", hasSize(0)));
  }

  @Test
  @DisplayName("POST /tester/all - Should handle null in list")
  void updateTesters_WithNullInList_ShouldReturnBadRequest() throws Exception {
    mockMvc
        .perform(
            post("/tester/all")
                .header("X-User-ID", "123")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[{\"name\":\"Valid\"}, null]"))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("POST /tester/all - Should fail when entity not found for update")
  void updateTesters_WhenEntityNotFound_ShouldReturnError() throws Exception {
    TesterDto updateDto =
        TesterDto.builder()
            .id(999999L)
            .name("Non-existent")
            .tags(Set.of(TesterTagType.TAG1))
            .version(0L)
            .build();

    mockMvc
        .perform(
            post("/tester/all")
                .header("X-User-ID", "123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(List.of(updateDto))))
        .andExpect(status().isInternalServerError());
  }

  @Test
  @DisplayName("POST /tester/all - Should handle optimistic locking conflict")
  void updateTesters_WithStaleVersion_ShouldReturnConflict() throws Exception {
    // Given - create existing tester
    Tester existingTester = new Tester();
    existingTester.setName("Original");
    existingTester.setTags(new HashSet<>(Set.of(TesterTagType.TAG1)));
    existingTester = testerRepository.save(existingTester);
    entityManager.flush();

    // Update it once to increment version
    existingTester.setName("Updated Once");
    testerRepository.save(existingTester);
    entityManager.flush();
    entityManager.clear();

    // Try to update with old version
    TesterDto staleDto =
        TesterDto.builder()
            .id(existingTester.getId())
            .name("Stale Update")
            .tags(Set.of(TesterTagType.TAG2))
            .version(0L) // Old version
            .build();

    mockMvc
        .perform(
            post("/tester/all")
                .header("X-User-ID", "123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(List.of(staleDto))))
        .andExpect(status().isConflict());
  }

  @Test
  @DisplayName("POST /tester/all - Should handle malformed JSON")
  void updateTesters_WithMalformedJson_ShouldReturnBadRequest() throws Exception {
    mockMvc
        .perform(
            post("/tester/all")
                .header("X-User-ID", "123")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json}"))
        .andExpect(status().isBadRequest());
  }
}
