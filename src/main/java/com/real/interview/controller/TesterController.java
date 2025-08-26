package com.real.interview.controller;

import com.real.interview.dto.tester.TesterDto;
import com.real.interview.service.TesterService;
import jakarta.validation.constraints.Min;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/tester")
public class TesterController {
  private final TesterService testerService;

  @GetMapping("/{id}")
  public Optional<TesterDto> getTest(@PathVariable @Min(1) final long id) {
    return testerService.getTest(id);
  }

  @PostMapping("/all")
  public List<TesterDto> updateTesters(@RequestBody final List<TesterDto> testerDtoList) {
    return testerService.updateTesters(testerDtoList);
  }
}
