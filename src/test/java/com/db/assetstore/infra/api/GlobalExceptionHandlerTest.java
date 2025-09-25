package com.db.assetstore.infra.api;

import com.db.assetstore.domain.exception.EventGenerationException;
import com.db.assetstore.domain.exception.TransformSchemaValidationException;
import com.db.assetstore.domain.exception.link.LinkCardinalityViolationException;
import com.db.assetstore.domain.exception.link.LinkDefinitionNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = GlobalExceptionHandlerTest.TestController.class)
@Import(GlobalExceptionHandler.class)
class GlobalExceptionHandlerTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void mapsDomainNotFoundTo404() throws Exception {
        mockMvc.perform(get("/test/not-found").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message", containsString("Link definition missing")));
    }

    @Test
    void mapsDomainConflictTo409() throws Exception {
        mockMvc.perform(get("/test/conflict").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message", containsString("Asset asset-1 already has")));
    }

    @Test
    void mapsDomainValidationTo422() throws Exception {
        mockMvc.perform(get("/test/validation").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.message", containsString("invalid")));
    }

    @Test
    void mapsDomainInternalTo500() throws Exception {
        mockMvc.perform(get("/test/internal").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message", containsString("boom")));
    }

    @RestController
    static class TestController {
        @GetMapping("/test/not-found")
        void notFound() throws Exception {
            throw new LinkDefinitionNotFoundException("WORKFLOW", "BULK");
        }

        @GetMapping("/test/conflict")
        void conflict() throws Exception {
            throw new LinkCardinalityViolationException("Asset asset-1 already has an active link");
        }

        @GetMapping("/test/validation")
        void validation() throws Exception {
            throw new TransformSchemaValidationException("Payload invalid");
        }

        @GetMapping("/test/internal")
        void internal() throws Exception {
            throw new EventGenerationException("boom", new RuntimeException("boom"));
        }
    }
}
