package backend.SQLGeneration.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.fasterxml.jackson.databind.ObjectMapper;

import backend.SQLGeneration.dto.SchemaDTO;
import backend.SQLGeneration.service.SchemaService;
import backend.SQLGeneration.service.util.SchemaValidationException;
import backend.security.AuthUser;

@ExtendWith(MockitoExtension.class)
class SchemaControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SchemaService schemaService;

    @InjectMocks
    private SchemaController schemaController;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AuthUser mockAuthUser = new AuthUser(1, "testuser");

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(schemaController)
                .setCustomArgumentResolvers(new HandlerMethodArgumentResolver() {
                    @Override
                    public boolean supportsParameter(MethodParameter parameter) {
                        return parameter.getParameterType().equals(AuthUser.class);
                    }

                    @Override
                    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                        return mockAuthUser;
                    }
                })
                .build();
    }

    @Test
    void generateDDL_success_returns200() throws Exception {
        SchemaDTO schemaDTO = new SchemaDTO();
        String mockDDL = "CREATE TABLE test (...)";
        
        when(schemaService.generateDDL(any(SchemaDTO.class))).thenReturn(mockDDL);

        mockMvc.perform(post("/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(schemaDTO)))
                .andExpect(status().isOk())
                .andExpect(content().string(mockDDL));
    }

    @Test
    void generateDDL_validationError_returns400() throws Exception {
        SchemaDTO schemaDTO = new SchemaDTO();
        String errorMsg = "Invalid schema";
        
        when(schemaService.generateDDL(any(SchemaDTO.class)))
                .thenThrow(new SchemaValidationException(errorMsg));

        mockMvc.perform(post("/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(schemaDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Schema validation error: " + errorMsg));
    }

    @Test
    void generateDDL_unexpectedError_returns500() throws Exception {
        SchemaDTO schemaDTO = new SchemaDTO();
        String errorMsg = "Something went wrong";
        
        when(schemaService.generateDDL(any(SchemaDTO.class)))
                .thenThrow(new RuntimeException(errorMsg));

        mockMvc.perform(post("/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(schemaDTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Unexpected error: " + errorMsg));
    }
}
