package backend.publicDiagramManagement.controller;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.fasterxml.jackson.databind.ObjectMapper;

import backend.publicDiagramManagement.dto.publish.PublishDiagramRequestDto;
import backend.publicDiagramManagement.service.PublicDiagramService;
import backend.security.AuthUser;

@ExtendWith(MockitoExtension.class)
class PublicDiagramControllerTest {

  private MockMvc mockMvc;

  @Mock
  private PublicDiagramService publicDiagramService;

  @InjectMocks
  private PublicDiagramController publicDiagramController;

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final AuthUser mockAuthUser = new AuthUser(1, "test-user");

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(publicDiagramController)
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
  void updateDiagram_success() throws Exception {
    PublishDiagramRequestDto requestDto = new PublishDiagramRequestDto();
    requestDto.setDiagramId(UUID.randomUUID());
    requestDto.setShortDescription("Update");

    doNothing().when(publicDiagramService).updatePublicDiagram(eq(1), any(PublishDiagramRequestDto.class));

    mockMvc.perform(put("/publicDiagrams/update")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(requestDto)))
        .andExpect(status().isOk())
        .andExpect(content().string("Diagram details updated successfully :)"));

    verify(publicDiagramService).updatePublicDiagram(eq(1), any(PublishDiagramRequestDto.class));
  }
}
