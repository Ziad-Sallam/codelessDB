package backend.SQLGeneration.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Supported SQL data types for generation")
public enum SQLTypeName {

    // --- Integer Types ---
    TINYINT,
    SMALLINT,
    MEDIUMINT,
    INT,
    INTEGER,
    BIGINT,

    // --- Fixed-Point Types ---
    DECIMAL,
    DEC,
    NUMERIC,
    FIXED,

    // --- Floating-Point Types ---
    FLOAT,
    DOUBLE,
    DOUBLE_PRECISION,
    REAL,

    // --- Bit ---
    BIT,
    BOOL,
    BOOLEAN,

    // --- Date and Time ---
    DATE,
    DATETIME,
    TIMESTAMP,
    TIME,
    YEAR,

    // --- String Types ---
    CHAR,
    VARCHAR,
    TINYTEXT,
    TEXT,
    MEDIUMTEXT,
    LONGTEXT,

    // --- Binary Types ---
    BINARY,
    VARBINARY,
    TINYBLOB,
    BLOB,
    MEDIUMBLOB,
    LONGBLOB,

    // --- Enum / Set Types ---
    ENUM,
    SET,

    // --- Spatial Types ---
    GEOMETRY,
    POINT,
    LINESTRING,
    POLYGON,
    MULTIPOINT,
    MULTILINESTRING,
    MULTIPOLYGON,
    GEOMETRYCOLLECTION
}