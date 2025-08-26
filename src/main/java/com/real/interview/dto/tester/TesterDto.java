package com.real.interview.dto.tester;

import com.real.interview.common.dto.VersionedDto;
import com.real.interview.domain.tester.TesterTagType;
import java.util.Set;
import lombok.Builder;

@Builder
public record TesterDto(Long id, Set<TesterTagType> tags, String name, long version)
    implements VersionedDto<Long> {}
