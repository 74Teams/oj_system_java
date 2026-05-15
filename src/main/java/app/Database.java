package app;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class Database {
    private static final String URL = "jdbc:sqlite:database.db";
    private static Connection connection;

    public static Connection getConnection() {

        try {

            if (connection == null || connection.isClosed()) {

                connection =
                        DriverManager.getConnection(URL);

                System.out.println("Database connected!");

                initDatabase();
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        return connection;
    }
    private static void initDatabase() {

        try {

            Statement stmt =
                    connection.createStatement();

            String problemsTable = """
                    CREATE TABLE IF NOT EXISTS problems (
                    
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                    
                        title TEXT NOT NULL,
                    
                        statement TEXT,
                    
                        input_spec TEXT,
                    
                        output_spec TEXT,
                    
                        constraints_text TEXT,
                    
                        time_limit_ms INTEGER DEFAULT 1000,
                    
                        memory_limit_mb INTEGER DEFAULT 256,
                    
                        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                    
                        updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
                    )
                    """;

            String testcasesTable = """
                    CREATE TABLE IF NOT EXISTS testcases (
                    
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                    
                        problem_id INTEGER NOT NULL,
                    
                        type TEXT DEFAULT 'normal',
                    
                        input TEXT,
                    
                        output TEXT,
                    
                        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                    
                        updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                    
                        FOREIGN KEY(problem_id)
                        REFERENCES problems(id)
                        ON DELETE CASCADE
                    )
                    """;

            stmt.execute(testcasesTable);
            stmt.execute(problemsTable);

            System.out.println("Database initialized!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
