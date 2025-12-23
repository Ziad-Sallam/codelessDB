package backend.publicDiagramManagement.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.fasterxml.jackson.databind.ObjectMapper;

import backend.publicDiagramManagement.dto.comment.CommentDto;
import backend.publicDiagramManagement.dto.comment.CommentRequestDto;
import backend.publicDiagramManagement.service.CommentService;
import backend.security.AuthUser;
import backend.security.GoogleSuccessHandler;
import backend.security.JwtExtractor;
import backend.security.JwtUtil;

@WebMvcTest(CommentController.class)
@AutoConfigureMockMvc(addFilters = false)
public class CommentControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private CommentController commentController;

    @MockitoBean
    private CommentService commentService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private JwtExtractor jwtExtractor;

    @MockitoBean
    private GoogleSuccessHandler googleSuccessHandler;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Create a mock argument resolver for AuthUser
        HandlerMethodArgumentResolver authUserResolver = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.getParameterType().equals(AuthUser.class);
            }

            @Override
            public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                    NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                return new AuthUser(123, "testuser");
            }
        };

        mockMvc = MockMvcBuilders.standaloneSetup(commentController)
                .setCustomArgumentResolvers(authUserResolver)
                .build();
    }

    @Test
    void getComments_Success() throws Exception {
        UUID diagramId = UUID.randomUUID();
        when(commentService.getComments(anyInt(), eq(diagramId))).thenReturn(List.of());

        mockMvc.perform(get("/publicDiagrams/" + diagramId + "/comments"))
                .andExpect(status().isOk());
    }

    @Test
    void addComment_Success() throws Exception {
        UUID diagramId = UUID.randomUUID();
        CommentRequestDto requestDto = new CommentRequestDto("Test Content", null);
        CommentDto responseDto = CommentDto.builder().content("Test Content").build();

        when(commentService.addComment(anyInt(), eq(diagramId), any(CommentRequestDto.class)))
                .thenReturn(responseDto);

        mockMvc.perform(post("/publicDiagrams/" + diagramId + "/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk());
    }

    @Test
    void updateComment_Success() throws Exception {
        Long commentId = 1L;
        CommentRequestDto requestDto = new CommentRequestDto("Updated Content", null);
        CommentDto responseDto = CommentDto.builder().content("Updated Content").build();

        when(commentService.updateComment(anyInt(), eq(commentId), any(CommentRequestDto.class)))
                .thenReturn(responseDto);

        mockMvc.perform(put("/publicDiagrams/comments/" + commentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk());
    }

    @Test
    void deleteComment_Success() throws Exception {
        Long commentId = 1L;

        mockMvc.perform(delete("/publicDiagrams/comments/" + commentId))
                .andExpect(status().isOk());
    }

    @Test
    void reactToComment_Success() throws Exception {
        Long commentId = 1L;
        CommentDto responseDto = CommentDto.builder().build();

        when(commentService.reactToComment(anyInt(), eq(commentId), eq("LIKE")))
                .thenReturn(responseDto);

        mockMvc.perform(post("/publicDiagrams/comments/" + commentId + "/react")
                .param("type", "LIKE"))
                .andExpect(status().isOk());
    }
}
