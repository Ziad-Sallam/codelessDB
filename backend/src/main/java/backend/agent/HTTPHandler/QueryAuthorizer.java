package backend.agent.HTTPHandler;

import backend.databaseManagement.exception.DatabaseException;
import backend.user.Role;

public class QueryAuthorizer {
    public static SqlOperation classifySql(String sql) {
        String q = sql.trim().toUpperCase();

        if (q.startsWith("SELECT"))
            return SqlOperation.SELECT;
        if (q.startsWith("INSERT"))
            return SqlOperation.INSERT;
        if (q.startsWith("UPDATE"))
            return SqlOperation.UPDATE;
        if (q.startsWith("DELETE"))
            return SqlOperation.DELETE;
        if (q.startsWith("MERGE"))
            return SqlOperation.MERGE;

        if (q.startsWith("CREATE"))
            return SqlOperation.CREATE;
        if (q.startsWith("DROP"))
            return SqlOperation.DROP;
        if (q.startsWith("ALTER"))
            return SqlOperation.ALTER;
        if (q.startsWith("TRUNCATE"))
            return SqlOperation.TRUNCATE;

        if (q.startsWith("GRANT"))
            return SqlOperation.GRANT;
        if (q.startsWith("REVOKE"))
            return SqlOperation.REVOKE;

        if (q.startsWith("BEGIN") || q.startsWith("START TRANSACTION"))
            return SqlOperation.BEGIN;
        if (q.startsWith("COMMIT"))
            return SqlOperation.COMMIT;
        if (q.startsWith("ROLLBACK"))
            return SqlOperation.ROLLBACK;
        if (q.startsWith("SAVEPOINT"))
            return SqlOperation.SAVEPOINT;

        if (q.startsWith("ANALYZE"))
            return SqlOperation.ANALYZE;
        if (q.startsWith("OPTIMIZE"))
            return SqlOperation.OPTIMIZE;
        if (q.startsWith("VACUUM"))
            return SqlOperation.VACUUM;
        if (q.startsWith("LOCK"))
            return SqlOperation.LOCK;

        if (q.startsWith("EXPLAIN"))
            return SqlOperation.EXPLAIN;
        if (q.startsWith("SHOW"))
            return SqlOperation.SHOW;
        if (q.startsWith("DESCRIBE"))
            return SqlOperation.DESCRIBE;

        return SqlOperation.UNKNOWN;
    }

    public static void validateSql(String sql) {
        String q = sql.trim().toUpperCase();

        if (q.contains(";")) {
            throw new DatabaseException.UnauthorizedAccessException(
                    "Multiple SQL statements are not allowed");
        }

        if (q.startsWith("CREATE DATABASE") || q.startsWith("DROP DATABASE")) {
            throw new DatabaseException.UnauthorizedAccessException(
                    "Database-level operations are not allowed");
        }

        if (q.startsWith("USE ")) {
            throw new DatabaseException.UnauthorizedAccessException(
                    "Switching databases is not allowed");
        }
    }

    public static void authorize(Role role, String sql) {
        SqlOperation op = classifySql(sql);

        switch (role) {

            case READER:
                if (!(op == SqlOperation.SELECT
                        || op == SqlOperation.EXPLAIN
                        || op == SqlOperation.SHOW
                        || op == SqlOperation.DESCRIBE)) {
                    throw new DatabaseException.UnauthorizedAccessException(
                            "READER can only execute read-only queries");
                }
                break;

            case WRITER:
                if (!(op == SqlOperation.SELECT
                        || op == SqlOperation.INSERT
                        || op == SqlOperation.UPDATE
                        || op == SqlOperation.DELETE
                        || op == SqlOperation.MERGE
                        || op == SqlOperation.BEGIN
                        || op == SqlOperation.COMMIT
                        || op == SqlOperation.ROLLBACK
                        || op == SqlOperation.SAVEPOINT)) {
                    throw new DatabaseException.UnauthorizedAccessException(
                            "WRITER cannot modify schema or permissions");
                }
                break;

            case OWNER:
                break;

        }
    }

}
