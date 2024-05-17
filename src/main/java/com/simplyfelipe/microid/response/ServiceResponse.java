package com.simplyfelipe.microid.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServiceResponse<T> {
    private int code;
    private String status;
    private String message;
    private T body;
}
