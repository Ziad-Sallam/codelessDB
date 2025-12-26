package backend.publicDiagramManagement.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

class PageResponseTest {

    @Test
    void testConstructorAndGetters() {
        List<String> content = List.of("A", "B");
        Page<String> page = new PageImpl<>(content, PageRequest.of(1, 2), 10);

        PageResponse<String> response = new PageResponse<>(page);

        assertEquals(content, response.getContent());
        assertEquals(1, response.getPage());
        assertEquals(2, response.getSize());
        assertEquals(10, response.getTotalElements());
        assertEquals(5, response.getTotalPages());
    }
}
