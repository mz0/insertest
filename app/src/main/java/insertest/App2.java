package insertest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;

public class App2 {
    public static void main(String[] args) throws IOException {
        Logger logger = LogManager.getLogger();
        if (args.length > 0) {
            logger.info("arg0 {}", args[0]);
        }
        Properties connectionProps = new Properties();
        try (FileInputStream fis = new FileInputStream("connection.properties")) {
            connectionProps.load(fis);
        }
        // use.url = jdbc:mysql://localhost:3306 / use.schema = shsha_Blue / use.table = test /
        // use.batch_size = 2500 / use.batches = 40000
        String schemaName = connectionProps.getProperty("use.schema");
        String jdbcUrl =  connectionProps.getProperty("use.url") + "/" + schemaName;
        String tableName = connectionProps.getProperty("use.table");
        boolean autoCommit = connectionProps.getProperty("use.autocommit").equalsIgnoreCase("TRUE");
        int insertRecords = Integer.parseInt(connectionProps.getProperty("use.batch_size"));
        int batches = Integer.parseInt(connectionProps.getProperty("use.batches"));
        for (Object k : connectionProps.keySet()) {
            if (((String) k).startsWith("use.")) {
                connectionProps.remove(k);
            }
        }
        logger.info("table {}, batch_size {}, batches {}, autoCommit {}", tableName, insertRecords, batches, autoCommit);
        Insert insE = new Insert(tableName, insertRecords);
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

            conn.setAutoCommit(autoCommit);
            t0 = System.currentTimeMillis();
            int pkStart = 1;
            for (int i = 0; i < batches; i++) {
                makeBatch(insSt, pkStart, insE.insertValues, insertRecords);
                insSt.addBatch();
                pkStart = pkStart + insertRecords;
            }
            int[] br = insSt.executeBatch();
            if (!autoCommit) { conn.commit(); }
            t1 = System.currentTimeMillis();
            MysqlStats stat1 = new MysqlStats(stmt.executeQuery(MysqlStats.mysqlRequest));
            logger.info(stat1.diffSTable(stat0));
            int nonZeroes = 0; int printMe = 5;
            int expectResult = insertRecords;
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
    private static void makeBatch(PreparedStatement st, int pkStart, List<Object> colValues, int batchSize) throws SQLException {
        int pk = pkStart;
        int argIdx = 0;
        for (int bIdx = 0; bIdx < batchSize; bIdx++) {
            // ++argIdx;
            // st.setInt(argIdx, pk);
            for (Object colValue : colValues) {
                ++argIdx;
                st.setObject(argIdx, colValue);
            }
            pk++;
        }
    }
}
