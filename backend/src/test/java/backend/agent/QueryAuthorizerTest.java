package backend.agent;

import backend.agent.HTTPHandler.QueryAuthorizer;
import backend.agent.HTTPHandler.SqlOperation;
import backend.databaseManagement.exception.DatabaseException;
import backend.user.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QueryAuthorizerTest {


    @Test
    @DisplayName("Classify SELECT")
    void classifySelect() {
        assertEquals(SqlOperation.SELECT,
                QueryAuthorizer.classifySql("select * from users"));
    }

    @Test
    void classifyInsert() {
        assertEquals(SqlOperation.INSERT,
                QueryAuthorizer.classifySql("INSERT INTO t VALUES (1)"));
    }

    @Test
    void classifyUpdate() {
        assertEquals(SqlOperation.UPDATE,
                QueryAuthorizer.classifySql("update t set a=1"));
    }

    @Test
    void classifyDelete() {
        assertEquals(SqlOperation.DELETE,
                QueryAuthorizer.classifySql("delete from t"));
    }

    @Test
    void classifyDDL() {
        assertEquals(SqlOperation.CREATE,
                QueryAuthorizer.classifySql("create table x(id int)"));
        assertEquals(SqlOperation.DROP,
                QueryAuthorizer.classifySql("drop table x"));
        assertEquals(SqlOperation.ALTER,
                QueryAuthorizer.classifySql("alter table x add a int"));
        assertEquals(SqlOperation.TRUNCATE,
                QueryAuthorizer.classifySql("truncate table x"));
    }

    @Test
    void calssifyRevoke() {
        assertEquals(SqlOperation.REVOKE,
                QueryAuthorizer.classifySql("revoke select on t from u"));
    }

    @Test
    void classifyTransactions() {
        assertEquals(SqlOperation.BEGIN,
                QueryAuthorizer.classifySql("begin"));
        assertEquals(SqlOperation.BEGIN,
                QueryAuthorizer.classifySql("start transaction"));
        assertEquals(SqlOperation.COMMIT,
                QueryAuthorizer.classifySql("commit"));
        assertEquals(SqlOperation.ROLLBACK,
                QueryAuthorizer.classifySql("rollback"));
        assertEquals(SqlOperation.SAVEPOINT,
                QueryAuthorizer.classifySql("savepoint s1"));
    }

    @Test
    void classifyAdmin() {
        assertEquals(SqlOperation.ANALYZE,
                QueryAuthorizer.classifySql("analyze table x"));
        assertEquals(SqlOperation.OPTIMIZE,
                QueryAuthorizer.classifySql("optimize table x"));
        assertEquals(SqlOperation.VACUUM,
                QueryAuthorizer.classifySql("vacuum"));
        assertEquals(SqlOperation.LOCK,
                QueryAuthorizer.classifySql("lock table x"));
    }

    @Test
    void classifyMetadata() {
        assertEquals(SqlOperation.EXPLAIN,
                QueryAuthorizer.classifySql("explain select * from t"));
        assertEquals(SqlOperation.SHOW,
                QueryAuthorizer.classifySql("show tables"));
        assertEquals(SqlOperation.DESCRIBE,
                QueryAuthorizer.classifySql("describe t"));
    }

    @Test
    void classifyUnknown() {
        assertEquals(SqlOperation.UNKNOWN,
                QueryAuthorizer.classifySql("random command"));
    }

    @Test
    void rejectMultipleStatements() {
        assertThrows(DatabaseException.UnauthorizedAccessException.class,
                () -> QueryAuthorizer.validateSql("SELECT * FROM t; DELETE FROM t"));
    }

    @Test
    void rejectCreateDatabase() {
        assertThrows(DatabaseException.UnauthorizedAccessException.class,
                () -> QueryAuthorizer.validateSql("CREATE DATABASE test"));
    }

    @Test
    void rejectDropDatabase() {
        assertThrows(DatabaseException.UnauthorizedAccessException.class,
                () -> QueryAuthorizer.validateSql("DROP DATABASE test"));
    }

    @Test
    void rejectUseDatabase() {
        assertThrows(DatabaseException.UnauthorizedAccessException.class,
                () -> QueryAuthorizer.validateSql("USE testdb"));
    }

    @Test
    void allowSafeQuery() {
        assertDoesNotThrow(() ->
                QueryAuthorizer.validateSql("SELECT * FROM users"));
    }

    @Test
    void readerCanSelect() {
        assertDoesNotThrow(() ->
                QueryAuthorizer.authorize(Role.READER, "SELECT * FROM t"));
    }

    @Test
    void readerCanExplainShowDescribe() {
        assertDoesNotThrow(() ->
                QueryAuthorizer.authorize(Role.READER, "EXPLAIN SELECT * FROM t"));
        assertDoesNotThrow(() ->
                QueryAuthorizer.authorize(Role.READER, "SHOW TABLES"));
        assertDoesNotThrow(() ->
                QueryAuthorizer.authorize(Role.READER, "DESCRIBE t"));
    }

    @Test
    void readerCannotWrite() {
        assertThrows(DatabaseException.UnauthorizedAccessException.class,
                () -> QueryAuthorizer.authorize(Role.READER, "INSERT INTO t VALUES (1)"));
    }


    @Test
    void writerCanWrite() {
        assertDoesNotThrow(() ->
                QueryAuthorizer.authorize(Role.WRITER, "INSERT INTO t VALUES (1)"));
        assertDoesNotThrow(() ->
                QueryAuthorizer.authorize(Role.WRITER, "UPDATE t SET a=1"));
        assertDoesNotThrow(() ->
                QueryAuthorizer.authorize(Role.WRITER, "DELETE FROM t"));
        assertDoesNotThrow(() ->
                QueryAuthorizer.authorize(Role.WRITER, "MERGE INTO t"));
        assertDoesNotThrow(() ->
                QueryAuthorizer.authorize(Role.WRITER, "BEGIN"));
        assertDoesNotThrow(() ->
                QueryAuthorizer.authorize(Role.WRITER, "COMMIT"));
        assertDoesNotThrow(() ->
                QueryAuthorizer.authorize(Role.WRITER, "ROLLBACK"));
        assertDoesNotThrow(() ->
                QueryAuthorizer.authorize(Role.WRITER, "SAVEPOINT s1"));
    }

    @Test
    void writerCanUseTransactions() {
        assertDoesNotThrow(() ->
                QueryAuthorizer.authorize(Role.WRITER, "BEGIN"));
        assertDoesNotThrow(() ->
                QueryAuthorizer.authorize(Role.WRITER, "COMMIT"));
        assertDoesNotThrow(() ->
                QueryAuthorizer.authorize(Role.WRITER, "ROLLBACK"));
        assertDoesNotThrow(() ->
                QueryAuthorizer.authorize(Role.WRITER, "SAVEPOINT s1"));
    }

    @Test
    void writerCannotDDL() {
        assertThrows(DatabaseException.UnauthorizedAccessException.class,
                () -> QueryAuthorizer.authorize(Role.WRITER, "CREATE TABLE x(id int)"));
    }


    @Test
    void ownerHasFullAccessInsideDB() {
        assertDoesNotThrow(() ->
                QueryAuthorizer.authorize(Role.OWNER, "CREATE TABLE x(id int)"));
        assertDoesNotThrow(() ->
                QueryAuthorizer.authorize(Role.OWNER, "DROP TABLE x"));
        assertDoesNotThrow(() ->
                QueryAuthorizer.authorize(Role.OWNER, "GRANT SELECT ON t TO u"));
        assertDoesNotThrow(() ->
                QueryAuthorizer.authorize(Role.OWNER, "ANALYZE"));
    }

}
