package backend.userDiagramManagment;

import backend.security.JwtUtil;
import backend.userDiagramManagement.controller.UserDiagramController;
import backend.userDiagramManagement.dto.*;
import backend.userDiagramManagement.dto.create.DiagramCreateRequestDto;
import backend.userDiagramManagement.dto.update.DiagramUpdateRequestDto;
import backend.userDiagramManagement.service.IUserDiagramService;
import backend.userDiagramManagement.exceptions.DiagramExceptionHandler;
import backend.security.AuthUser;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserDiagramController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(DiagramExceptionHandler.class)
public class UserDiagramControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IUserDiagramService userDiagramService;

    @MockitoBean
    private JwtUtil jwtUtil;

    private static final ObjectMapper mapper = new ObjectMapper();

    private String toJson(Object obj) throws Exception {
        return mapper.writeValueAsString(obj);
    }

    private void setAuthentication(AuthUser authUser) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(authUser, null, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    // ---------------- Create diagram ----------------
    @Nested
    @DisplayName("Create Diagram API")
    class CreateTests {

        @Test
        void testCreateSuccess() throws Exception {
            when(userDiagramService.createDiagram(anyInt(), any(DiagramCreateRequestDto.class)))
                    .thenReturn(UUID.randomUUID());

            AuthUser auth = new AuthUser(1, "john");
            setAuthentication(auth);

            try {
                DiagramCreateRequestDto dto = new DiagramCreateRequestDto();
                dto.setName("Test Diagram");

                mockMvc.perform(post("/diagram/create")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(toJson(dto)))
                        .andExpect(status().isOk());
            } finally {
                clearAuthentication();
            }
        }
    }

    // ---------------- Update diagram ----------------
    @Nested
    @DisplayName("Update Diagram API")
    class UpdateTests {

        @Test
        void testUpdateSuccess() throws Exception {
            when(userDiagramService.updateDiagram(anyInt(), any(DiagramUpdateRequestDto.class)))
                    .thenReturn(new Date());

            AuthUser auth = new AuthUser(1, "john");
            setAuthentication(auth);

            try {
                DiagramUpdateRequestDto dto = new DiagramUpdateRequestDto();
                UUID id = UUID.randomUUID();
                dto.setId(id);

                mockMvc.perform(put("/diagram/update/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(toJson(dto)))
                        .andExpect(status().isOk());
            } finally {
                clearAuthentication();
            }
        }
    }

    // ---------------- Delete diagram ----------------
    @Nested
    @DisplayName("Delete Diagram API")
    class DeleteTests {

        @Test
        void testDeleteSuccess() throws Exception {
            doNothing().when(userDiagramService).deleteDiagram(anyInt(), any(UUID.class));

            AuthUser auth = new AuthUser(1, "john");
            setAuthentication(auth);

            try {
                mockMvc.perform(delete("/diagram/delete/{id}", UUID.randomUUID()))
                        .andExpect(status().isOk());
            } finally {
                clearAuthentication();
            }
        }
    }

    // ---------------- Search diagram by id ----------------
    @Nested
    @DisplayName("Search Diagram by ID API")
    class SearchByIdTests {

        @Test
        void testSearchByIdSuccess() throws Exception {
            DiagramDto dto = new DiagramDto(
                    UUID.randomUUID(), "name", "desc", new byte[0], new Date(), new Date(), "john");
            when(userDiagramService.searchDiagramById(anyInt(), any(UUID.class)))
                    .thenReturn(dto);

            AuthUser auth = new AuthUser(1, "john");
            setAuthentication(auth);

            try {
                mockMvc.perform(get("/diagram/search/{id}", UUID.randomUUID()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.name").value("name"));
            } finally {
                clearAuthentication();
            }
        }
    }
}
