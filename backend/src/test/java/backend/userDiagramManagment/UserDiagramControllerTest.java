package backend.userDiagramManagment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import backend.security.AuthUser;
import backend.userDiagramManagement.controller.UserDiagramController;
import backend.userDiagramManagement.dto.DiagramDto;
import backend.userDiagramManagement.dto.DiagramInfoDto;
import backend.userDiagramManagement.dto.create.DiagramCreateRequestDto;
import backend.userDiagramManagement.dto.search.DiagramSearchRequestDto;
import backend.userDiagramManagement.dto.share.DiagramShareRequestDto;
import backend.userDiagramManagement.dto.share.DiagramShareResponseDto;
import backend.userDiagramManagement.dto.update.DiagramUpdateRequestDto;
import backend.userDiagramManagement.exceptions.DiagramException;
import backend.userDiagramManagement.exceptions.DiagramExceptionHandler;
import backend.userDiagramManagement.service.IUserDiagramService;

@ExtendWith(MockitoExtension.class)
class UserDiagramControllerTest {

    private MockMvc mockMvc;

    @Mock
    private IUserDiagramService userDiagramService;

    @InjectMocks
    private UserDiagramController userDiagramController;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private final AuthUser mockAuthUser = new AuthUser(1, "john");

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userDiagramController)
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
                .setControllerAdvice(new DiagramExceptionHandler())
                .build();
    }

    @Test
    void getDiagramsByUserId_success() throws Exception {
        Page<DiagramInfoDto> page = new PageImpl<>(List.of());
        when(userDiagramService.getDiagramsByUserId(eq(1), any())).thenReturn(page);

        mockMvc.perform(get("/diagrams/get")
                        .param("pageNumber", "0")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void createDiagram_success() throws Exception {
        DiagramCreateRequestDto request = new DiagramCreateRequestDto();
        DiagramInfoDto response = DiagramInfoDto.builder().name("New Diagram").build();
        when(userDiagramService.createDiagram(eq(1), any())).thenReturn(response);

        mockMvc.perform(post("/diagrams/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Diagram"));
    }

    @Test
    void updateDiagram_success() throws Exception {
        UUID diagramId = UUID.randomUUID();
        DiagramUpdateRequestDto request = new DiagramUpdateRequestDto();
        LocalDateTime updateDate = LocalDateTime.now();
        when(userDiagramService.updateDiagram(eq(1), any(), eq(diagramId))).thenReturn(updateDate);

        mockMvc.perform(put("/diagrams/update/" + diagramId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Diagram updated successfully"))
                .andExpect(jsonPath("$.diagramId").value(diagramId.toString()));
    }

    @Test
    void deleteDiagram_success() throws Exception {
        UUID diagramId = UUID.randomUUID();

        mockMvc.perform(delete("/diagrams/delete/" + diagramId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Diagram deleted successfully"))
                .andExpect(jsonPath("$.diagramId").value(diagramId.toString()));
    }

    @Test
    void deleteDiagram_forbidden_publicDiagram() throws Exception {
        UUID diagramId = UUID.randomUUID();
        doThrow(new DiagramException.PermissionDeniedException("Can't delete public diagram, Unpublished it first"))
                .when(userDiagramService).deleteDiagram(eq(1), eq(diagramId));

        mockMvc.perform(delete("/diagrams/delete/" + diagramId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Can't delete public diagram, Unpublished it first"));
    }

    @Test
    void searchDiagramById_success() throws Exception {
        UUID diagramId = UUID.randomUUID();
        DiagramDto response = DiagramDto.builder().id(diagramId).name("Found").build();
        when(userDiagramService.searchDiagramById(eq(1), eq(diagramId))).thenReturn(response);

        mockMvc.perform(get("/diagrams/search/" + diagramId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Found"));
    }

    @Test
    void searchDiagrams_success() throws Exception {
        DiagramSearchRequestDto request = new DiagramSearchRequestDto();
        Page<DiagramInfoDto> page = new PageImpl<>(List.of());
        when(userDiagramService.searchDiagrams(eq(1), any(), any())).thenReturn(page);

        mockMvc.perform(post("/diagrams/search")
                        .param("pageNumber", "0")
                        .param("pageSize", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void shareDiagram_success() throws Exception {
        UUID diagramId = UUID.randomUUID();
        DiagramShareRequestDto request = new DiagramShareRequestDto();
        DiagramShareResponseDto response = new DiagramShareResponseDto("Success", "mike", null, null);
        when(userDiagramService.shareDiagram(eq(1), eq(diagramId), any())).thenReturn(response);

        mockMvc.perform(put("/diagrams/share/" + diagramId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success"));
    }

    @Test
    void shareDiagram_invalidData_returns400() throws Exception {
        UUID diagramId = UUID.randomUUID();
        DiagramShareRequestDto request = new DiagramShareRequestDto();
        when(userDiagramService.shareDiagram(eq(1), eq(diagramId), any()))
                .thenThrow(new DiagramException.InvalidDiagramDataException("Error message"));

        mockMvc.perform(put("/diagrams/share/" + diagramId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error message"));
    }

    @Test
    void updateDDL_success() throws Exception {
        UUID diagramId = UUID.randomUUID();
        String ddl = "CREATE TABLE users (id INT PRIMARY KEY);";

        doNothing().when(userDiagramService).updateDDL(eq(diagramId), eq(ddl));

        mockMvc.perform(put("/diagrams/update-ddl/" + diagramId)
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(ddl))
                .andExpect(status().isOk());
    }

    @Test
    void getDDL_success() throws Exception {
        UUID diagramId = UUID.randomUUID();
        String ddl = "CREATE TABLE users (id INT PRIMARY KEY);";

        when(userDiagramService.getDDL(eq(diagramId))).thenReturn(ddl);

        mockMvc.perform(get("/diagrams/get-ddl/" + diagramId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(ddl));
    }

}