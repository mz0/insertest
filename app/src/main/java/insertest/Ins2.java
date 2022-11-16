package insertest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.Callable;

public class Ins2 implements Callable<String> {
    private final String tableName;
    private final Insert insE;
    private final int insertRecords;
    private Connection conn;
    private final boolean autoCommit;
    private final int batches;
    private final Logger log;

    public Ins2(String tableName, int insertRecords, Connection conn, boolean autoCommit, int batches) {
        this.tableName = tableName;
        this.insertRecords = insertRecords;
        this.insE = new Insert(tableName, insertRecords);
        this.conn = conn;
        this.autoCommit = autoCommit;
        this.batches = batches;
        this.log = LogManager.getLogger(Ins2.class.getSimpleName() + "-" + tableName);
    }

    @Override
    public String call() {
        try (Statement stmt = conn.createStatement(); PreparedStatement insSt = conn.prepareStatement(insE.insert)) {
            long t_A = System.currentTimeMillis();
            int tf = stmt.executeUpdate(insE.dropTable);
            long t_B = System.currentTimeMillis();
            log.info("{}: {} ms - DROP Table result is {}", Thread.currentThread().getName(), t_B - t_A, tf);
            tf = stmt.executeUpdate(insE.createTable);
            t_A = System.currentTimeMillis();
            log.info("{} ms - Create Table result is {}", t_A - t_B, tf);
            t_A = System.currentTimeMillis();
            conn.setAutoCommit(autoCommit);
            int pkStart = 1;
            for (int i = 0; i < batches; i++) {
                makeBatch(insSt, pkStart, insE.insertValues, insertRecords);
                insSt.addBatch();
                pkStart = pkStart + insertRecords;
            }
            int[] br = insSt.executeBatch();

            if (!autoCommit) {
                conn.commit();
            }
            t_B = System.currentTimeMillis();
            int unexpected = 0;
            int expectResult = insertRecords;
            for (int res : br) {
                if (res != expectResult) {
                    unexpected++;
                }
            }
            String summary = unexpected == 0 ? "all " + expectResult : String.format("%d unexpected", unexpected);
            return String.format("%s : %d ms - Insert result has length %d (%s)\n", tableName, t_B - t_A, br.length, summary);
        } catch (SQLException e) {
            return tableName + ": " + e.getMessage();
        }
    }

    private static void makeBatch(PreparedStatement st, int pkStart, List<Object> colValues, int batchSize) throws SQLException {
        int pk = pkStart;
        int argIdx = 0;
        for (int bIdx = 0; bIdx < batchSize; bIdx++) {
            ++argIdx;
            st.setInt(argIdx, pk);
            for (Object colValue : colValues) {
                ++argIdx;
                st.setObject(argIdx, colValue);
            }
            pk++;
        }
    }
}
