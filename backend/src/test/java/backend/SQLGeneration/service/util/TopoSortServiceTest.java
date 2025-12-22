package backend.SQLGeneration.service.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import backend.SQLGeneration.dto.AttributeDTO;
import backend.SQLGeneration.dto.EntityDTO;
import backend.SQLGeneration.dto.constraint.ForeignKeyConstraintDTO;

class TopoSortServiceTest {

    private TopoSortService topoSortService;

    @BeforeEach
    void setUp() {
        topoSortService = new TopoSortService();
    }

    @Test
    void sortEntitiesByDependencies_noDependencies_returnsSameEntities() {
        EntityDTO e1 = new EntityDTO("User", null);
        EntityDTO e2 = new EntityDTO("Post", null);
        List<EntityDTO> entities = Arrays.asList(e1, e2);

        List<EntityDTO> result = topoSortService.sortEntitiesByDependencies(entities);

        assertEquals(2, result.size());
        assertTrue(result.contains(e1));
        assertTrue(result.contains(e2));
    }

    @Test
    void sortEntitiesByDependencies_simpleDependency_returnsCorrectOrder() {
        // User depends on Role
        ForeignKeyConstraintDTO fk = new ForeignKeyConstraintDTO("Role", "id", null, null);
        AttributeDTO attr = new AttributeDTO("role_id", null, false, Collections.singletonList(fk), false);
        EntityDTO user = new EntityDTO("User", Collections.singletonList(attr));
        
        EntityDTO role = new EntityDTO("Role", null);

        // Sorting [User, Role] should give [Role, User]
        List<EntityDTO> entities = Arrays.asList(user, role);
        List<EntityDTO> result = topoSortService.sortEntitiesByDependencies(entities);

        assertEquals(2, result.size());
        assertEquals("Role", result.get(0).getName());
        assertEquals("User", result.get(1).getName());
    }

    @Test
    void sortEntitiesByDependencies_circularDependency_throwsException() {
        // A depends on B
        ForeignKeyConstraintDTO fkA = new ForeignKeyConstraintDTO("B", "id", null, null);
        AttributeDTO attrA = new AttributeDTO("b_id", null, false, Collections.singletonList(fkA), false);
        EntityDTO a = new EntityDTO("A", Collections.singletonList(attrA));

        // B depends on A
        ForeignKeyConstraintDTO fkB = new ForeignKeyConstraintDTO("A", "id", null, null);
        AttributeDTO attrB = new AttributeDTO("a_id", null, false, Collections.singletonList(fkB), false);
        EntityDTO b = new EntityDTO("B", Collections.singletonList(attrB));

        List<EntityDTO> entities = Arrays.asList(a, b);

        SchemaValidationException ex = assertThrows(SchemaValidationException.class, 
            () -> topoSortService.sortEntitiesByDependencies(entities));
        assertEquals("Circular foreign key dependency detected", ex.getMessage());
    }

    @Test
    void sortEntitiesByDependencies_missingTableReference_throwsException() {
        ForeignKeyConstraintDTO fk = new ForeignKeyConstraintDTO("Missing", "id", null, null);
        AttributeDTO attr = new AttributeDTO("m_id", null, false, Collections.singletonList(fk), false);
        EntityDTO a = new EntityDTO("A", Collections.singletonList(attr));

        List<EntityDTO> entities = Collections.singletonList(a);

        SchemaValidationException ex = assertThrows(SchemaValidationException.class, 
            () -> topoSortService.sortEntitiesByDependencies(entities));
        assertTrue(ex.getMessage().contains("table 'Missing' does not exist"));
    }

    @Test
    void sortEntitiesByDependencies_selfDependency_ignored() {
        // A depends on A (self-reference)
        ForeignKeyConstraintDTO fk = new ForeignKeyConstraintDTO("A", "id", null, null);
        AttributeDTO attr = new AttributeDTO("id", null, false, Collections.singletonList(fk), false);
        EntityDTO a = new EntityDTO("A", Collections.singletonList(attr));

        List<EntityDTO> entities = Collections.singletonList(a);

        List<EntityDTO> result = assertDoesNotThrow(() -> topoSortService.sortEntitiesByDependencies(entities));
        assertEquals(1, result.size());
        assertEquals("A", result.get(0).getName());
    }
}
