package backend.SQLGeneration.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;

class SQLDataTypeTest {

    @Test
    void toDDL_Varchar() {
        SQLDataType type = new SQLDataType(SQLTypeName.VARCHAR, 255, null, null, null);
        assertEquals("VARCHAR(255)", type.toDDL());
    }

    @Test
    void toDDL_Decimal() {
        SQLDataType type = new SQLDataType(SQLTypeName.DECIMAL, null, 12, 2, null);
        assertEquals("DECIMAL(12,2)", type.toDDL());
    }

    @Test
    void toDDL_Decimal_Defaults() {
        SQLDataType type = new SQLDataType(SQLTypeName.DECIMAL, null, null, null, null);
        assertEquals("DECIMAL(10,0)", type.toDDL());
    }

    @Test
    void toDDL_Enum_Valid() {
        SQLDataType type = new SQLDataType(SQLTypeName.ENUM, null, null, null, Arrays.asList("A", "B"));
        assertEquals("ENUM('A','B')", type.toDDL());
    }

    @Test
    void toDDL_Enum_Empty() {
        SQLDataType type = new SQLDataType(SQLTypeName.ENUM, null, null, null, Collections.emptyList());
        assertEquals("ENUM()", type.toDDL());
    }

    @Test
    void toDDL_Enum_Null() {
        SQLDataType type = new SQLDataType(SQLTypeName.ENUM, null, null, null, null);
        assertEquals("ENUM()", type.toDDL());
    }

    @Test
    void toDDL_Integer() {
        SQLDataType type = new SQLDataType(SQLTypeName.INT, null, null, null, null);
        assertEquals("INT", type.toDDL());
    }
}
