package com.educaflow.common.db;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

public class BulkTables {

    public void truncateTables(String dataBaseDriver, String dataBaseURL, String dataBaseUser, String dataBasePassword, String schemaName, Set<String> tablasExcluidas, Set<String> tablasIncluidas)  {

        try {
            System.out.println("Conectando a la base de datos...");
            Class.forName(dataBaseDriver);
            try (Connection connection = DriverManager.getConnection(dataBaseURL, dataBaseUser, dataBasePassword)) {
                connection.setAutoCommit(false);

                System.out.println("Desactivando restricciones...");
                disableAllTriggers(connection, schemaName);

                truncateTablesByLike(connection, schemaName, "meta\\_%", tablasExcluidas);
                truncateTablesByLike(connection, schemaName, "auth\\_%", tablasExcluidas);
                truncateTablesByName(connection, tablasIncluidas);


                enableAllTriggers(connection, schemaName);

                connection.commit();
                System.out.println("Operación completada exitosamente.");
            } catch (Exception ex) {
                throw new RuntimeException("Fallo al conectar a la base de datos", ex);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Fallo al borrar los meta datos", ex);
        }
    }

    private void disableAllTriggers(Connection connection, String schemaName) throws SQLException {
        Tables tables = new Tables(connection, schemaName);
        List<String> allTableNames = tables.getAllTableNames();

        for (String tableName : allTableNames) {
            Table table = new Table(connection, tableName);
            table.disableAllTriggers();
        }
    }


    private void enableAllTriggers(Connection connection, String schemaName) throws SQLException {
        Tables tables = new Tables(connection, schemaName);
        List<String> allTableNames = tables.getAllTableNames();

        for (String tableName : allTableNames) {
            Table table = new Table(connection, tableName);
            table.enablebleAllTriggers();
        }
    }


    private void truncateTablesByLike(Connection connection, String schemaName, String like, Set<String> tablasExcluidas) {
        System.out.println("Borrando contenido de las tablas que empiezan por '" + like + "' .....");
        Tables tables = new Tables(connection, schemaName);
        List<String> metaTables = tables.getTableNamesByLike(like);

        for (String tableName : metaTables) {
            if (!tablasExcluidas.contains(tableName)) {
                System.out.println("Borrando contenido de la tabla:" + tableName);
                Table table = new Table(connection, tableName);
                table.truncate();
            }
        }

    }

    private void truncateTablesByName(Connection connection, Set<String> tablasIncluidas) {

        for (String tableName : tablasIncluidas) {
            System.out.println("Borrando contenido de la tabla:" + tableName);
            Table table = new Table(connection, tableName);
            table.truncate();
        }

    }

}


