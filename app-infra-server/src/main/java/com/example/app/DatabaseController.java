package com.example.app;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/database")
@CrossOrigin(origins = "*")
public class DatabaseController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

   // @PostConstruct
    private void postConstruct() {
        String sql = "drop table mock_event_details";
        try {
            jdbcTemplate.execute(sql);
        } catch (DataAccessException ex) {

        }
        sql = "drop table api_event_details";
        try {
            jdbcTemplate.execute(sql);
        } catch (DataAccessException ex) {

        }
        sql = """
                create table  api_event_details
                (
                    event_id    int              not null,
                    event_status boolean default false ,
                    event_score  VARCHAR(255)
                )
                """;
        jdbcTemplate.execute(sql);
        sql = """
                create table  mock_event_details
                (
                    event_id    int primary key ,
                    event_status boolean default false,
                    event_score  VARCHAR(255)
                )
                """;
        jdbcTemplate.execute(sql);
    }


    @GetMapping("/tables")
    public List<Map<String, Object>> getAllTables() {
        List<Map<String, Object>> tables = new ArrayList<>();

        try {
            Connection connection = jdbcTemplate.getDataSource().getConnection();
            DatabaseMetaData metaData = connection.getMetaData();

            // Get table information for the current schema
            ResultSet rs = metaData.getTables(null, null, "%", new String[]{"TABLE"});

            while (rs.next()) {
                Map<String, Object> table = new HashMap<>();
                String tableName = rs.getString("TABLE_NAME");
                table.put("name", tableName);

                // Get row count for each table
                try {
                    String countQuery = "SELECT COUNT(*) FROM " + tableName;
                    Long rowCount = jdbcTemplate.queryForObject(countQuery, Long.class);
                    table.put("rowCount", rowCount != null ? rowCount : 0);
                } catch (Exception e) {
                    table.put("rowCount", 0);
                }

                tables.add(table);
            }

            rs.close();
            connection.close();

        } catch (Exception e) {
            // Return error information
            Map<String, Object> error = new HashMap<>();
            error.put("name", "Error");
            error.put("rowCount", 0);
            tables.add(error);

            // Log the error
            System.err.println("Error fetching tables: " + e.getMessage());
        }

        return tables;
    }

    @PostMapping("/query")
    public Map<String, Object> executeQuery(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();

        String query = request.get("query");
        if (query == null || query.trim().isEmpty()) {
            response.put("error", "Query cannot be empty");
            return response;
        }

        try {
            // Check if it's a SELECT query
            String trimmedQuery = query.trim().toUpperCase();
            if (trimmedQuery.startsWith("SELECT")) {
                List<Map<String, Object>> results = jdbcTemplate.queryForList(query);
                response.put("results", results);
                response.put("message", "Query executed successfully");
            } else {
                // For non-SELECT queries (INSERT, UPDATE, DELETE)
                int rowsAffected = jdbcTemplate.update(query);
                response.put("rowsAffected", rowsAffected);
                response.put("message", "Query executed successfully. " + rowsAffected + " rows affected.");
            }

        } catch (Exception e) {
            response.put("error", "Query execution failed: " + e.getMessage());
            System.err.println("Query execution error: " + e.getMessage());
        }

        return response;
    }

    @GetMapping("/table/{tableName}")
    public Map<String, Object> getTableData(@PathVariable String tableName) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Get table structure
            Connection connection = jdbcTemplate.getDataSource().getConnection();
            DatabaseMetaData metaData = connection.getMetaData();

            // Get column information
            ResultSet columns = metaData.getColumns(null, null, tableName, null);
            List<Map<String, Object>> columnInfo = new ArrayList<>();

            while (columns.next()) {
                Map<String, Object> column = new HashMap<>();
                column.put("name", columns.getString("COLUMN_NAME"));
                column.put("type", columns.getString("TYPE_NAME"));
                column.put("size", columns.getInt("COLUMN_SIZE"));
                column.put("nullable", columns.getString("IS_NULLABLE"));
                columnInfo.add(column);
            }

            columns.close();

            // Get sample data (first 100 rows)
            String query = "SELECT * FROM " + tableName + " FETCH FIRST 100 ROWS ONLY";
            List<Map<String, Object>> data = jdbcTemplate.queryForList(query);

            response.put("columns", columnInfo);
            response.put("data", data);
            response.put("tableName", tableName);

            connection.close();

        } catch (Exception e) {
            response.put("error", "Failed to get table data: " + e.getMessage());
            System.err.println("Table data error: " + e.getMessage());
        }

        return response;
    }
}
