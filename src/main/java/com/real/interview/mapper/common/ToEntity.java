package com.real.interview.mapper.common;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.mapstruct.Mapping;

@Retention(RetentionPolicy.CLASS)
@Mapping(target = "auditInfo.createdDate", ignore = true)
@Mapping(target = "auditInfo.createdBy", ignore = true)
@Mapping(target = "auditInfo.lastModifiedDate", ignore = true)
@Mapping(target = "auditInfo.lastModifiedBy", ignore = true)
public @interface ToEntity {}
