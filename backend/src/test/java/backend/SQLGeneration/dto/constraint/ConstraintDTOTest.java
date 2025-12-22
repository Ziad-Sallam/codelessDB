package backend.SQLGeneration.dto.constraint;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import backend.SQLGeneration.dto.ConstraintDTO;

class ConstraintDTOTest {

    @Test
    void testPrimaryKeyConstraint() {
        PrimaryKeyConstraintDTO pk = new PrimaryKeyConstraintDTO();
        assertEquals("PRIMARY KEY", pk.toSQL());
        assertEquals("PRIMARY KEY", pk.toSQL("anyColumn"));
    }

    @Test
    void testNotNullConstraint() {
        NotNullConstraintDTO notNull = new NotNullConstraintDTO();
        assertEquals("NOT NULL", notNull.toSQL());
    }

    @Test
    void testUniqueConstraint() {
        UniqueConstraintDTO unique = new UniqueConstraintDTO();
        assertEquals("UNIQUE", unique.toSQL());
    }

    @Test
    void testCheckConstraint() {
        CheckConstraintDTO check = new CheckConstraintDTO("age > 18");
        assertEquals("CHECK (age > 18)", check.toSQL());
        
        check.setExpression("salary > 0");
        assertEquals("salary > 0", check.getExpression());
    }

    @Test
    void testDefaultConstraint() {
        DefaultConstraintDTO def = new DefaultConstraintDTO("0");
        assertEquals("DEFAULT 0", def.toSQL());
        
        def.setDefaultValue("'N/A'");
        assertEquals("'N/A'", def.getDefaultValue());
    }

    @Test
    void testConstraintDTO_DefaultBehavior() {
        ConstraintDTO anon = new ConstraintDTO() {};
        assertEquals("", anon.toSQL());
        assertEquals("", anon.toSQL("column"));
    }
}
