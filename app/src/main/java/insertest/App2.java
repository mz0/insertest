package insertest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;

public class App2 {
    public static void main(String[] args) {
        Logger logger = LogManager.getLogger();
        String schemaName = "shsha_Blue";
        String userName = "shsha";
        String password = "999";
        String serverName = "localhost";
        String portNumber = String.valueOf(3306);
        String dbms = "mysql";
        String jdbcUrl =  "jdbc:" + dbms + "://" + serverName + ":" + portNumber + "/" + schemaName;
        Properties connectionProps = new Properties();
        connectionProps.put("user", userName);
        connectionProps.put("password", password);
        connectionProps.put("rewriteBatchedStatements", "OFF");
        connectionProps.put("sslMode", "DISABLED");
        connectionProps.put("characterEncoding", "latin1");
        connectionProps.put("createDatabaseIfNotExist", "true");
        connectionProps.put("useServerPrepStmts", "true");
        Insert insE = new Insert("test", (short) 1);
        long t0 = System.currentTimeMillis();
        try (Connection conn = DriverManager.getConnection(jdbcUrl, connectionProps);
             PreparedStatement insSt = conn.prepareStatement(insE.insert);
             Statement stmt = conn.createStatement()
        ) {
            long t1 = System.currentTimeMillis();
            int tf = stmt.executeUpdate(insE.dropTable);
            System.out.printf("%d ms - DROP Table result is %d\n", t1 - t0, tf);
            t0 = System.currentTimeMillis();
            tf = stmt.executeUpdate(insE.createTable);
            t1 = System.currentTimeMillis();
            System.out.printf("%d ms - Create Table result is %d\n", t1 - t0, tf);
            MysqlStats stat0 = new MysqlStats(stmt.executeQuery(MysqlStats.mysqlRequest));
            conn.setAutoCommit(false);
            t0 = System.currentTimeMillis();
            for (int i = 1; i <= 100_000; i++) {
                setParams(insSt, i, insE.insertValues);
                insSt.addBatch();
            }
            int[] br = insSt.executeBatch();
            conn.commit();
            t1 = System.currentTimeMillis();
            MysqlStats stat1 = new MysqlStats(stmt.executeQuery(MysqlStats.mysqlRequest));
            logger.info(stat1.diffSTable(stat0));
            int nonZeroes = 0; int printMe = 5;
            int expectResult = -2;
            for (int res : br) {
                if (res != expectResult) {
                    nonZeroes++;
                    if (printMe > 0) {
                        System.out.println(res);  printMe--;
                    }
                }
            }
            String summary = nonZeroes == 0 ? "all " + expectResult: String.format("%d non-default", nonZeroes);
            System.out.printf("%d ms - Insert result has length %d (%s)\n", t1 - t0, br.length, summary);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    private static void setParams(PreparedStatement st, int pk, List<Object> colValues) throws SQLException {
        st.setInt(1, pk);
        int idx = 1;
        for (Object colValue : colValues) {
            ++idx;
            st.setObject(idx, colValue);
        }
    }
}
