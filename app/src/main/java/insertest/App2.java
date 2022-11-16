package insertest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class App2 {
    public static void main(String[] args) throws IOException, InterruptedException {
        Logger logger = LogManager.getLogger();
        if (args.length > 0) {
            logger.info("arg0 {}", args[0]);
        }
        Properties connectionProps = new Properties();
        try (FileInputStream fis = new FileInputStream("connection.properties")) {
            connectionProps.load(fis);
        }
        // use.url = jdbc:mysql://localhost:3306 / use.schema = sha_Blue / use.table = test /
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
        logger.info("table test[12], batch_size {}, batches {}, autoCommit {}", insertRecords, batches, autoCommit);
        try (Connection conn1 = DriverManager.getConnection(jdbcUrl, connectionProps);
             Connection conn2 = DriverManager.getConnection(jdbcUrl, connectionProps);
             Statement stmt = conn1.createStatement()
        ) {
            MysqlStats stat0 = new MysqlStats(stmt.executeQuery(MysqlStats.mysqlRequest));
            final ExecutorService threads = Executors.newFixedThreadPool(2);
            final ExecutorCompletionService<String> ecs = new ExecutorCompletionService<>(threads);
            final long start = System.currentTimeMillis();
            ecs.submit(new Ins2("test1", insertRecords, conn1, autoCommit, batches/2));
            ecs.submit(new Ins2("test2", insertRecords, conn2, autoCommit, batches/2));
            threads.shutdown(); // no more tasks accepted
            for (int k = 1; k < 3; k++) {
                final Future<String> future = ecs.take();
                try {
                    final String summary = future.get();
                    logger.info(summary);
                } catch (ExecutionException e) {
                    logger.error("Error", e.getCause());
                }
            }
            threads.shutdownNow();
            System.out.printf("inserters done in %d ms%n", System.currentTimeMillis() - start);
            MysqlStats stat1 = new MysqlStats(stmt.executeQuery(MysqlStats.mysqlRequest));
            logger.info(stat1.diffSTable(stat0));
        } catch (SQLException e) {
            logger.error(e);
        }
        logger.info("done");
    }
}
