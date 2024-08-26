package tech.turso.libsql;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;

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
    public void failNestedTransaction() {
        try {
            try (var db = Libsql.open(":memory:");
                 var conn = db.connect()) {
                var tx1 = conn.transaction();
                var tx2 = tx1.transaction();
            }
            fail("Successfully made a nested transaction");
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
            conn.query("select :a", Map.of("a", 1));
        }
    }

    @Test
    public void queryRows() {
        try (var db = Libsql.open(":memory:");
                var conn = db.connect()) {
            try (var rows = conn.query("select 1", Map.of("a", 1))) {
                assertEquals(1L, rows.next().get(0));
            }
        }
    }

    @Test
    public void queryMultiple() {
        try (var db = Libsql.open(":memory:");
             var conn = db.connect()) {

            var end = 255;

            conn.execute("create table test (i integer, t text, r real, b blob)");

            for (int i = 0; i < 255; i++) {
                conn.execute(
                        "insert into test values (?, ?, ?, ?)",
                        i, "" + i, Math.exp(i), new byte[]{ (byte) i }
                );
            }

            try (var rows = conn.query("select * from test")) {
                var i = 0;
                for (var row : rows) {
                    assertEquals((long) i, row.get(0));
                    assertEquals("" + i, row.get(1));
                    assertEquals(Math.exp(i), row.get(2));
                    assertArrayEquals(new byte[]{ (byte) i }, (byte[]) row.get(3));
                    i++;
                }
            }
        }
    }

    @Test
    public void queryPositionalParameters() {
        try (var db = Libsql.open(":memory:");
                var conn = db.connect()) {
            conn.query("select ?", 1);
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
            conn.execute("insert into test values(:a)", Map.of("a", 1));
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
    public void executeBatchList() {
        try (var db = Libsql.open(":memory:");
                var conn = db.connect()) {
            conn.executeBatch(
                    List.of(
                            "create table test(i integer)",
                            "insert into test values(1)",
                            "insert into test values(2)",
                            "insert into test values(3)"));
        }
    }

    @Test
    public void executePositionalParameters() {
        try (var db = Libsql.open(":memory:");
                var conn = db.connect()) {
            conn.execute("create table test(i integer)");
            conn.execute("insert into test values(?)", 1);
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
            tx.query("select :a", Map.of("a", 1));
            tx.commit();
        }
    }

    @Test
    public void transactionQueryRows() {
        try (var db = Libsql.open(":memory:");
                var conn = db.connect()) {
            var tx = conn.transaction();
            try (var rows = tx.query("select 1", Map.of("a", 1))) {
                assertEquals(1L, rows.next().get(0));
            }
            tx.commit();
        }
    }

    @Test
    public void transactionQueryPositionalParameters() {
        try (var db = Libsql.open(":memory:");
                var conn = db.connect()) {
            var tx = conn.transaction();
            conn.query("select ?", 1);
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
            tx.execute("insert into test values(:a)", Map.of("a", 1));
            tx.commit();
        }
    }

    @Test
    public void transactionExecutePositionalParameters() {
        try (var db = Libsql.open(":memory:");
                var conn = db.connect()) {
            var tx = conn.transaction();
            tx.execute("create table test(i integer)");
            tx.execute("insert into test values(?)", 1);
            tx.commit();
        }
    }
}
