package backend.user;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class RoleTest {

    @Test
    void roleEnumValues() {
        Role[] roles = Role.values();
        assertEquals(3, roles.length);
        assertEquals(Role.READER, Role.valueOf("READER"));
        assertEquals(Role.WRITER, Role.valueOf("WRITER"));
        assertEquals(Role.OWNER, Role.valueOf("OWNER"));
    }
}
