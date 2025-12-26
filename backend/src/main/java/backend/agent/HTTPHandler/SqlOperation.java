package backend.agent.HTTPHandler;

public enum SqlOperation {

    // DML
    SELECT,
    INSERT,
    UPDATE,
    DELETE,
    MERGE,

    // DDL (inside DB)
    CREATE,
    DROP,
    ALTER,
    TRUNCATE,

    // DCL
    GRANT,
    REVOKE,

    // Transactions
    BEGIN,
    COMMIT,
    ROLLBACK,
    SAVEPOINT,

    // Admin
    ANALYZE,
    OPTIMIZE,
    VACUUM,
    LOCK,

    // Read-only metadata
    EXPLAIN,
    SHOW,
    DESCRIBE,

    UNKNOWN
}
