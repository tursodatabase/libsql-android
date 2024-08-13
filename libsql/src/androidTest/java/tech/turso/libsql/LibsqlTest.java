package tech.turso.libsql;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import androidx.test.ext.junit.runners.AndroidJUnit4;
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
    public void queryEmptyParameters() {
        try (var db = Libsql.open(":memory:");
             var conn = db.connect() ) {
            conn.query("select 1");
        }
    }

    @Test
    public void queryNamedParameters() {
        try (var db = Libsql.open(":memory:");
                var conn = db.connect() ) {
            conn.query("select :a", Map.of("a", Value.newBuilder().setInteger(1).build()));
        }
    }

    @Test
    public void queryPositionalParameters() {
        try (var db = Libsql.open(":memory:");
                var conn = db.connect() ) {
            conn.query("select ?", Value.newBuilder().setInteger(1).build());
        }
    }

    @Test
    public void executeEmptyParameters() {
        try (var db = Libsql.open(":memory:");
             var conn = db.connect() ) {
            conn.execute("create table test(i integer)");
        }
    }

    @Test
    public void executeNamedParameters() {
        try (var db = Libsql.open(":memory:");
             var conn = db.connect() ) {
            conn.execute("create table test(i integer)");
            conn.execute("insert into test values(:a)", Map.of("a", Value.newBuilder().setInteger(1).build()));
        }
    }

    @Test
    public void executePositionalParameters() {
        try (var db = Libsql.open(":memory:");
             var conn = db.connect() ) {
            conn.execute("create table test(i integer)");
            conn.execute("insert into test values(?)", Value.newBuilder().setInteger(1).build());
        }
    }
}
