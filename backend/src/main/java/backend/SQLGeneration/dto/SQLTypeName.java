package backend.SQLGeneration.dto;

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