package tech.turso.libsql;

import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class LibsqlTest {
    @Test
    public void openDatabase() {
        try (var db = Libsql.openLocal(":memory:")) {
        }
    }
}