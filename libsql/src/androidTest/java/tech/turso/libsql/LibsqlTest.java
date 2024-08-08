package tech.turso.libsql;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class LibsqlTest {
    @Test
    public void openDatabase() throws Exception {
        try (var db = Libsql.openLocal(":memory:");
                var conn = db.connect(); ) {
            conn.query("select :a", Map.of("a", Value.newBuilder().setInteger(1).build()));
        }
    }
}
