package backend.collab.snapshot;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import backend.security.AuthUser;
import backend.user.Role;

class SnapshotControllerTest {

	private MockMvc mockMvc;

	private SnapshotService snapshotService;

	private final UUID diagramId = UUID.randomUUID();
	private final AuthUser authUser = new AuthUser(100, "testuser");

	@org.junit.jupiter.api.BeforeEach
	void setUp() {
		snapshotService = mock(SnapshotService.class);
		mockMvc = MockMvcBuilders.standaloneSetup(new SnapshotController(snapshotService))
				.setCustomArgumentResolvers(new org.springframework.web.method.support.HandlerMethodArgumentResolver() {
					@Override
					public boolean supportsParameter(org.springframework.core.MethodParameter parameter) {
						return parameter.hasParameterAnnotation(
								org.springframework.security.core.annotation.AuthenticationPrincipal.class);
					}

					@Override
					public Object resolveArgument(org.springframework.core.MethodParameter parameter,
							org.springframework.web.method.support.ModelAndViewContainer mavContainer,
							org.springframework.web.context.request.NativeWebRequest webRequest,
							org.springframework.web.bind.support.WebDataBinderFactory binderFactory) {
						return authUser;
					}
				})
				.build();
	}

	@Test
	void testGetDiagramMetadata() throws Exception {
		SnapshotDto dto = new SnapshotDto("My Diagram", Role.WRITER);

		when(snapshotService.getDiagramMetadata(anyInt(), any(UUID.class)))
				.thenReturn(dto);

		mockMvc.perform(get("/snapshot/" + diagramId + "/meta"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.diagramName").value("My Diagram"))
				.andExpect(jsonPath("$.role").value("WRITER"));
	}

	@Test
	void testGetDiagramSnapshot() throws Exception {
		byte[] content = new byte[] { 1, 2, 3 };

		when(snapshotService.getDiagramSnapshot(eq(100), any(UUID.class)))
				.thenReturn(content);

		mockMvc.perform(get("/snapshot/" + diagramId + "/binary"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
				.andExpect(content().bytes(content));
	}
}
