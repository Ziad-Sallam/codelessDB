package backend.publicDiagramManagement.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.fasterxml.jackson.databind.ObjectMapper;

import backend.publicDiagramManagement.dto.PublicDiagramDto;
import backend.publicDiagramManagement.dto.PublicDiagramInfoDto;
import backend.publicDiagramManagement.dto.get.ToBePublishedDiagramDto;
import backend.publicDiagramManagement.dto.publish.PublishDiagramRequestDto;
import backend.publicDiagramManagement.dto.search.SearchRequestDto;
import backend.publicDiagramManagement.dto.user.PublicUserInfoDto;
import backend.publicDiagramManagement.exceptions.PublicDiagramExceptionHandler;
import backend.publicDiagramManagement.service.HashtagService;
import backend.publicDiagramManagement.service.PublicDiagramService;
import backend.security.AuthUser;

@ExtendWith(MockitoExtension.class)
class PublicDiagramControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PublicDiagramService publicDiagramService;

    @Mock
    private HashtagService hashtagService;

    @InjectMocks
    private PublicDiagramController publicDiagramController;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AuthUser mockAuthUser = new AuthUser(1, "testuser");

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
                .setControllerAdvice(new PublicDiagramExceptionHandler())
                .build();
    }

    @Test
    void publishDiagram() throws Exception {
        PublishDiagramRequestDto dto = new PublishDiagramRequestDto();
        dto.setDiagramId(UUID.randomUUID());

        mockMvc.perform(post("/publicDiagrams/publish")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Diagram published successfully :)"));

        verify(publicDiagramService).publishDiagram(eq(mockAuthUser.userId()), any());
    }

    @Test
    void getToBePublishDiagramsByUserId() throws Exception {
        Page<ToBePublishedDiagramDto> page = new PageImpl<>(List.of());
        when(publicDiagramService.getToBePublishedDiagrams(anyInt(), any())).thenReturn(page);

        mockMvc.perform(get("/publicDiagrams/getToBePublished")
                        .param("pageNumber", "0")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getPublicDiagram() throws Exception {
        UUID diagramId = UUID.randomUUID();
        PublicDiagramDto dto = PublicDiagramDto.builder().build();
        when(publicDiagramService.viewPublicDiagram(anyInt(), any())).thenReturn(dto);

        mockMvc.perform(get("/publicDiagrams/view/" + diagramId))
                .andExpect(status().isOk());
    }

    @Test
    void forkPublicDiagram() throws Exception {
        UUID diagramId = UUID.randomUUID();

        mockMvc.perform(post("/publicDiagrams/fork/" + diagramId))
                .andExpect(status().isOk())
                .andExpect(content().string("Diagram forked successfully :)"));

        verify(publicDiagramService).forkPublicDiagram(mockAuthUser.userId(), diagramId);
    }

    @Test
    void starPublicDiagram() throws Exception {
        UUID diagramId = UUID.randomUUID();

        mockMvc.perform(post("/publicDiagrams/star/" + diagramId))
                .andExpect(status().isOk())
                .andExpect(content().string("Diagram stared successfully :)"));

        verify(publicDiagramService).starPublicDiagram(mockAuthUser.userId(), diagramId);
    }

    @Test
    void unstarPublicDiagram() throws Exception {
        UUID diagramId = UUID.randomUUID();

        mockMvc.perform(delete("/publicDiagrams/unstar/" + diagramId))
                .andExpect(status().isOk())
                .andExpect(content().string("Diagram stared successfully :)"));

        verify(publicDiagramService).unstarPublicDiagram(mockAuthUser.userId(), diagramId);
    }

    @Test
    void getForkedDiagram() throws Exception {
        Page<PublicDiagramInfoDto> page = new PageImpl<>(List.of());
        when(publicDiagramService.getForkedPublicDiagrams(anyInt(), any())).thenReturn(page);

        mockMvc.perform(get("/publicDiagrams/forkedDiagrams"))
                .andExpect(status().isOk());
    }

    @Test
    void getStaredDiagram() throws Exception {
        Page<PublicDiagramInfoDto> page = new PageImpl<>(List.of());
        when(publicDiagramService.getStaredPublicDiagrams(anyInt(), any())).thenReturn(page);

        mockMvc.perform(post("/publicDiagrams/staredDiagrams"))
                .andExpect(status().isOk());
    }

    @Test
    void searchPublicDiagram() throws Exception {
        SearchRequestDto dto = new SearchRequestDto();
        Page<PublicDiagramInfoDto> page = new PageImpl<>(List.of());
        when(publicDiagramService.searchPublicDiagrams(any(), any())).thenReturn(page);

        mockMvc.perform(post("/publicDiagrams/searchDiagrams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void searchPublicUsers() throws Exception {
        SearchRequestDto dto = new SearchRequestDto();
        Page<PublicUserInfoDto> page = new PageImpl<>(List.of());
        when(publicDiagramService.searchUsersByPublicDiagrams(anyInt(), any(), any())).thenReturn(page);

        mockMvc.perform(post("/publicDiagrams/searchUsers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void getHashtags() throws Exception {
        when(hashtagService.getAll()).thenReturn(List.of("tag1"));

        mockMvc.perform(get("/publicDiagrams/hashtags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("tag1"));
    }

    @Test
    void unPublishPublicDiagram() throws Exception {
        UUID diagramId = UUID.randomUUID();

        mockMvc.perform(delete("/publicDiagrams/unpublish/" + diagramId))
                .andExpect(status().isOk())
                .andExpect(content().string("The Public Diagram has been deleted successfully :)"));

        verify(publicDiagramService).unPublishPublicDiagram(mockAuthUser.userId(), diagramId);
    }
}
