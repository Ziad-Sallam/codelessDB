package backend.publicDiagramManagement.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import backend.publicDiagramManagement.dto.PublicDiagramInfoDto;
import backend.publicDiagramManagement.dto.user.PublicUserDto;
import backend.publicDiagramManagement.dto.user.PublicUserFollowDto;
import backend.publicDiagramManagement.exceptions.PublicDiagramExceptionHandler;
import backend.publicDiagramManagement.service.PublicUserService;
import backend.security.AuthUser;

@ExtendWith(MockitoExtension.class)
class PublicUserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PublicUserService publicUserService;

    @InjectMocks
    private PublicUserController publicUserController;

    private final AuthUser mockAuthUser = new AuthUser(1, "testuser");
    private final AtomicReference<AuthUser> authUserRef = new AtomicReference<>(mockAuthUser);

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(publicUserController)
                .setCustomArgumentResolvers(new HandlerMethodArgumentResolver() {
                    @Override
                    public boolean supportsParameter(MethodParameter parameter) {
                        return parameter.getParameterType().equals(AuthUser.class);
                    }

                    @Override
                    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                        return authUserRef.get();
                    }
                })
                .setControllerAdvice(new PublicDiagramExceptionHandler())
                .build();
    }

    @Test
    void getDesignerProfile() throws Exception {
        authUserRef.set(mockAuthUser);
        PublicUserDto dto = PublicUserDto.builder().username("john").build();
        when(publicUserService.getDesignerProfile(eq(1), eq("john"))).thenReturn(dto);

        mockMvc.perform(get("/publicUsers/designerProfile/john"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("john"));
    }

    @Test
    void getDesignerProfile_noAuth() throws Exception {
        authUserRef.set(null);
        PublicUserDto dto = PublicUserDto.builder().username("john").build();
        when(publicUserService.getDesignerProfile(isNull(), eq("john"))).thenReturn(dto);

        mockMvc.perform(get("/publicUsers/designerProfile/john"))
                .andExpect(status().isOk());
    }

    @Test
    void getPublicDiagrams() throws Exception {
        Page<PublicDiagramInfoDto> page = new PageImpl<>(List.of());
        when(publicUserService.getPublicDiagrams(eq("john"), any())).thenReturn(page);

        mockMvc.perform(get("/publicUsers/publicDiagrams/john"))
                .andExpect(status().isOk());
    }

    @Test
    void getStaredDiagram() throws Exception {
        Page<PublicDiagramInfoDto> page = new PageImpl<>(List.of());
        when(publicUserService.getStaredPublicDiagrams(eq("john"), any())).thenReturn(page);

        mockMvc.perform(get("/publicUsers/staredDiagrams/john"))
                .andExpect(status().isOk());
    }

    @Test
    void followUser() throws Exception {
        mockMvc.perform(post("/publicUsers/follow/john"))
                .andExpect(status().isOk())
                .andExpect(content().string("john is followed successfully :)"));

        verify(publicUserService).followUser(mockAuthUser.userId(), "john");
    }

    @Test
    void unfollowUser() throws Exception {
        mockMvc.perform(delete("/publicUsers/unfollow/john"))
                .andExpect(status().isOk())
                .andExpect(content().string("john is unfollowed successfully :)"));

        verify(publicUserService).unfollowUser(mockAuthUser.userId(), "john");
    }

    @Test
    void getFollowers() throws Exception {
        Page<PublicUserFollowDto> page = new PageImpl<>(List.of());
        when(publicUserService.getFollowersByUsername(eq("john"), any())).thenReturn(page);

        mockMvc.perform(get("/publicUsers/followers/john"))
                .andExpect(status().isOk());
    }

    @Test
    void getFollowings() throws Exception {
        Page<PublicUserFollowDto> page = new PageImpl<>(List.of());
        when(publicUserService.getFollowingsByUsername(eq("john"), any())).thenReturn(page);

        mockMvc.perform(get("/publicUsers/followings/john"))
                .andExpect(status().isOk());
    }
}
