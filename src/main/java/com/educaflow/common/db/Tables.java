package com.educaflow.common.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Tables {

    private final Connection connection;
    private final String schemaName;

    public Tables(Connection connection, String schemaName) {
        this.connection = connection;
        this.schemaName = schemaName;
    }

    public List<String> getTableNamesByLike(String like) {
        String sql="SELECT tablename FROM pg_tables WHERE schemaname = ? AND tablename LIKE ?";

        List<String> tableNames = new ArrayList<>();

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, schemaName);
            preparedStatement.setString(2, like);
            ResultSet rsTables = preparedStatement.executeQuery();
            while (rsTables.next()) {
                tableNames.add(rsTables.getString("tablename"));
            }
            rsTables.close();
            return tableNames;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public List<String> getAllTableNames() {
        String sql="SELECT tablename FROM pg_tables WHERE schemaname = ?";

        List<String> tableNames = new ArrayList<>();

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, schemaName);
            ResultSet rsTables = preparedStatement.executeQuery();
            while (rsTables.next()) {
                tableNames.add(rsTables.getString("tablename"));
            }
            rsTables.close();
            return tableNames;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public boolean existsTable(String tableName) {
        String sql="SELECT tablename FROM pg_tables WHERE schemaname = ? AND tablename = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, schemaName);
            preparedStatement.setString(2, tableName);
            ResultSet rsTables = preparedStatement.executeQuery();

            boolean exists = rsTables.next();
            rsTables.close();

            return exists;

        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

}
