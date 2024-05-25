package com.simplyfelipe.microid;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.simplyfelipe.microid.response.ServiceResponse;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.http.HttpStatus;

import java.io.File;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class BaseTest {

    protected static ObjectMapper objectMapper;

    @BeforeAll
    protected static void init() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    protected <T> T readFile(String filePath, TypeReference<T> typeReference) throws Exception {
        return objectMapper.readValue(new File(filePath), typeReference);
    }

    protected <T> void assertResponseWithBody(ServiceResponse<T> actualResponse, T expectedBody, HttpStatus expectedStatus) {
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getCode()).isEqualTo(expectedStatus.value());
        assertThat(actualResponse.getStatus()).isEqualTo(expectedStatus.name());
        assertThat(actualResponse.getBody()).isEqualTo(expectedBody);
    }

    protected <S, T extends Collection<S>> void assertResponseWithBodySize(ServiceResponse<T> actualResponse,
                                                                           Collection<S> expectedBody,
                                                                           HttpStatus expectedStatus,
                                                                           int expectedSize) {
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getCode()).isEqualTo(expectedStatus.value());
        assertThat(actualResponse.getStatus()).isEqualTo(expectedStatus.name());
        assertThat(actualResponse.getBody()).isEqualTo(expectedBody);
        assertThat(actualResponse.getBody().size()).isEqualTo(expectedBody.size()).isEqualTo(expectedSize);
    }

    protected <T> void assertResponseWithMessage(ServiceResponse<T> actualResponse, String expectedMessage, HttpStatus expectedStatus) {
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getCode()).isEqualTo(expectedStatus.value());
        assertThat(actualResponse.getStatus()).isEqualTo(expectedStatus.name());
        assertThat(actualResponse.getMessage()).isEqualTo(expectedMessage);
        assertThat(actualResponse.getBody()).isNull();
    }
}
