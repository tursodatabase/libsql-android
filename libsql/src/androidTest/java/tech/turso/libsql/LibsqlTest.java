package tech.turso.libsql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import tech.turso.libsql.proto.Value;

@RunWith(AndroidJUnit4.class)
public class LibsqlTest {
    @Test
    public void failCloseTwoTimes() {
        try {
            var db = Libsql.open(":memory:");
            db.close();
            db.close();
            fail("Successfully closed the same Database two times");
        } catch (Throwable e) {
            assertNotNull(e);
        }
    }

    @Test
    public void queryEmptyParameters() {
        try (var db = Libsql.open(":memory:");
                var conn = db.connect()) {
            conn.query("select 1");
        }
    }

    @Test
    public void queryNamedParameters() {
        try (var db = Libsql.open(":memory:");
                var conn = db.connect()) {
            conn.query("select :a", Map.of("a", Value.newBuilder().setInteger(1).build()));
        }
    }

    @Test
    public void queryRows() {
        try (var db = Libsql.open(":memory:");
                var conn = db.connect()) {
            try (var rows =
                    conn.query("select 1", Map.of("a", Value.newBuilder().setInteger(1).build()))) {
                assertEquals(rows.next(), List.of(Value.newBuilder().setInteger(1).build()));
            }
        }
    }

    @Test
    public void queryPositionalParameters() {
        try (var db = Libsql.open(":memory:");
                var conn = db.connect()) {
            conn.query("select ?", Value.newBuilder().setInteger(1).build());
        }
    }

    @Test
    public void executeEmptyParameters() {
        try (var db = Libsql.open(":memory:");
                var conn = db.connect()) {
            conn.execute("create table test(i integer)");
        }
    }

    @Test
    public void executeNamedParameters() {
        try (var db = Libsql.open(":memory:");
                var conn = db.connect()) {
            conn.execute("create table test(i integer)");
            conn.execute(
                    "insert into test values(:a)",
                    Map.of("a", Value.newBuilder().setInteger(1).build()));
        }
    }

    @Test
    public void executeBatch() {
        try (var db = Libsql.open(":memory:");
                var conn = db.connect()) {
            conn.executeBatch(
                    "create table test(i integer);"
                            + "insert into test values(1);"
                            + "insert into test values(2);"
                            + "insert into test values(3);");
        }
    }

    @Test
    public void executePositionalParameters() {
        try (var db = Libsql.open(":memory:");
                var conn = db.connect()) {
            conn.execute("create table test(i integer)");
            conn.execute("insert into test values(?)", Value.newBuilder().setInteger(1).build());
        }
    }

    @Test
    public void transactionQueryEmptyParameters() {
        try (var db = Libsql.open(":memory:");
                var conn = db.connect()) {
            var tx = conn.transaction();
            tx.query("select 1");
            tx.commit();
        }
    }

    @Test
    public void transactionQueryNamedParameters() {
        try (var db = Libsql.open(":memory:");
                var conn = db.connect(); ) {
            var tx = conn.transaction();
            tx.query("select :a", Map.of("a", Value.newBuilder().setInteger(1).build()));
            tx.commit();
        }
    }

    @Test
    public void transactionQueryRows() {
        try (var db = Libsql.open(":memory:");
                var conn = db.connect()) {
            var tx = conn.transaction();
            try (var rows =
                    tx.query("select 1", Map.of("a", Value.newBuilder().setInteger(1).build()))) {
                assertEquals(rows.next(), List.of(Value.newBuilder().setInteger(1).build()));
            }
            tx.commit();
        }
    }

    @Test
    public void transactionQueryPositionalParameters() {
        try (var db = Libsql.open(":memory:");
                var conn = db.connect()) {
            var tx = conn.transaction();
            conn.query("select ?", Value.newBuilder().setInteger(1).build());
            tx.commit();
        }
    }

    @Test
    public void transactionExecuteEmptyParameters() {
        try (var db = Libsql.open(":memory:");
                var conn = db.connect()) {
            var tx = conn.transaction();
            tx.execute("create table test(i integer)");
            tx.commit();
        }
    }

    @Test
    public void transactionExecuteNamedParameters() {
        try (var db = Libsql.open(":memory:");
                var conn = db.connect()) {
            var tx = conn.transaction();
            tx.execute("create table test(i integer)");
            tx.execute(
                    "insert into test values(:a)",
                    Map.of("a", Value.newBuilder().setInteger(1).build()));
            tx.commit();
        }
    }

    @Test
    public void transactionExecutePositionalParameters() {
        try (var db = Libsql.open(":memory:");
                var conn = db.connect()) {
            var tx = conn.transaction();
            tx.execute("create table test(i integer)");
            tx.execute("insert into test values(?)", Value.newBuilder().setInteger(1).build());
            tx.commit();
        }
    }
}
