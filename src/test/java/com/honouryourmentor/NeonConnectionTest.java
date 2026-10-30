package com.honouryourmentor;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("neon")
class NeonConnectionTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void testNeonDatabaseConnection() throws Exception {
        assertNotNull(dataSource, "DataSource should not be null");
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT current_database(), current_user, version()")) {

            assertTrue(rs.next(), "Should return at least one row");
            String db = rs.getString(1);
            String user = rs.getString(2);
            String version = rs.getString(3);

            System.out.println("==================================================");
            System.out.println("✅ SUCCESSFULLY CONNECTED TO NEON POSTGRESQL!");
            System.out.println("Database : " + db);
            System.out.println("User     : " + user);
            System.out.println("Version  : " + version);
            System.out.println("==================================================");

            assertEquals("neondb", db);
            assertTrue(user.contains("neondb_owner"));
        }
    }
}

