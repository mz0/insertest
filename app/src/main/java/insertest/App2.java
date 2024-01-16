package insertest;

import insertest.structure.Dictionary;
import insertest.structure.StoredUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import static insertest.StatementMaker.createTable;
import static insertest.StatementMaker.dropTable;

public class App2 {
    public static void main(String[] args) {
        Logger logger = LogManager.getLogger();
        String schemaName = "ushat";
        String userName = "shsha";
        String password = "999";
        String serverName = "localhost";
        String portNumber = String.valueOf(3306);
        String dbms = "mysql";
        String jdbcUrl =  "jdbc:" + dbms + "://" + serverName + ":" + portNumber + "/" + schemaName;
        Properties connectionProps = new Properties();
        connectionProps.put("user", userName);
        connectionProps.put("password", password);
        connectionProps.put("rewriteBatchedStatements", "NO");
        connectionProps.put("sslMode", "DISABLED");
        connectionProps.put("characterEncoding", "latin1");
        connectionProps.put("createDatabaseIfNotExist", "true");
        connectionProps.put("useServerPrepStmts", "true");

        try (Connection conn = DriverManager.getConnection(jdbcUrl, connectionProps);
             Statement stmt = conn.createStatement()
        ) {
            MysqlStats stat0 = new MysqlStats(stmt.executeQuery(MysqlStats.mysqlRequest));
            for (StoredUnit mSample : Dictionary.getMessageSamples()) {
                String stmText = dropTable(mSample);
                stmt.executeUpdate(stmText);
                logger.info("{} OK", stmText);
                stmt.executeUpdate(createTable(mSample));
                logger.info("CREATE Table {} OK", mSample.getTableName());
                // conn.setAutoCommit(false);
                // conn.commit();
            }
            MysqlStats stat1 = new MysqlStats(stmt.executeQuery(MysqlStats.mysqlRequest));
            logger.info(stat1.diffSTable(stat0));
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
