package com.honouryourmentor;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClearAllTablesDirect {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:postgresql://ep-withered-sun-aocowwnw-pooler.c-2.ap-southeast-1.aws.neon.tech:5432/neondb?sslmode=require";
        String user = "neondb_owner";
        String pass = "npg_TlHCD8bisoE3";

        System.out.println("Connecting to Neon database...");
        try (Connection conn = DriverManager.getConnection(url, user, pass);
             Statement stmt = conn.createStatement()) {
            System.out.println("✅ Connected successfully!");

            // 1. Get all base tables in public schema
            List<String> tablesToClear = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery(
                    "SELECT table_name FROM information_schema.tables " +
                    "WHERE table_schema = 'public' AND table_type = 'BASE TABLE' ORDER BY table_name")) {
                while (rs.next()) {
                    String tbl = rs.getString(1);
                    if (!"flyway_schema_history".equalsIgnoreCase(tbl)) {
                        tablesToClear.add(tbl);
                    }
                }
            }

            System.out.println("\n--- BEFORE TRUNCATE ---");
            for (String tbl : tablesToClear) {
                try (ResultSet countRs = stmt.executeQuery("SELECT count(*) FROM \"" + tbl + "\"")) {
                    if (countRs.next()) {
                        System.out.printf("  %s: %d rows%n", tbl, countRs.getLong(1));
                    }
                }
            }

            // 2. Execute TRUNCATE CASCADE on all application tables
            if (!tablesToClear.isEmpty()) {
                StringBuilder truncateSql = new StringBuilder("TRUNCATE TABLE ");
                for (int i = 0; i < tablesToClear.size(); i++) {
                    if (i > 0) truncateSql.append(", ");
                    truncateSql.append("\"").append(tablesToClear.get(i)).append("\"");
                }
                truncateSql.append(" CASCADE;");

                System.out.println("\nExecuting SQL: " + truncateSql);
                stmt.execute(truncateSql.toString());
                System.out.println("✅ Truncation complete!");
            }

            System.out.println("\n--- AFTER TRUNCATE ---");
            for (String tbl : tablesToClear) {
                try (ResultSet countRs = stmt.executeQuery("SELECT count(*) FROM \"" + tbl + "\"")) {
                    if (countRs.next()) {
                        System.out.printf("  %s: %d rows%n", tbl, countRs.getLong(1));
                    }
                }
            }

            // Also check flyway_schema_history is intact
            try (ResultSet countRs = stmt.executeQuery("SELECT count(*) FROM flyway_schema_history")) {
                if (countRs.next()) {
                    System.out.printf("  flyway_schema_history: %d rows (preserved)%n", countRs.getLong(1));
                }
            }
        }
    }
}
