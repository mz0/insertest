package insertest;

import com.mysql.cj.jdbc.MysqlDataSource;
import insertest.pipeline.Pipeline;
import insertest.structure.ConnectOpts;
import insertest.structure.Dictionary;
import insertest.structure.StoredUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import static insertest.Datasources.MYSQL_DEFAULT_URL;
import static insertest.StatementMaker.createTable;
import static insertest.StatementMaker.dropTable;

public class App2 {
    public static void main(String[] args) {
        Logger logger = LogManager.getLogger();
        var ds = new MysqlDataSource();
        Path propFile = Paths.get("db.properties");
		try (FileInputStream fis = new FileInputStream(propFile.toFile())) {
            var props = new Properties();
            props.load(fis);
            ds.setURL(props.getProperty("db.url", MYSQL_DEFAULT_URL));
            ds.setUser(props.getProperty("db.user", "shsha"));
            ds.setPassword(props.getProperty("db.password", "999"));
        } catch (FileNotFoundException e) {
            logger.warn("{} not found. Loading default JDBC URL, user, password", propFile.toAbsolutePath());
            ds.setURL(MYSQL_DEFAULT_URL);
            ds.setUser("shsha");
            ds.setPassword("999");
        } catch (IOException e) {
            throw new RuntimeException("error reading " + propFile.toAbsolutePath());
        }

        // drop & make a new table for each "message" type (using samples from the Dictionary)
        try (Connection conn = ds.getConnection(); Statement stmt = conn.createStatement()) {
            var stat0 = new MysqlStats(stmt.executeQuery(MysqlStats.mysqlRequest));
            for (StoredUnit mSample : Dictionary.getMessageSamples()) {
                String stmText = dropTable(mSample);
                stmt.executeUpdate(stmText);
                logger.info("{} OK", stmText);
                stmt.executeUpdate(createTable(mSample));
                logger.info("CREATE Table {} OK", mSample.getTableName());
            }
            var stat1 = new MysqlStats(stmt.executeQuery(MysqlStats.mysqlRequest));
            logger.info(stat1.diffSTable(stat0));
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }

        new Pipeline(
                new ConnectOpts(ds.getURL(), ds.getUser(), ds.getPassword()),
                Dictionary.numberOfTypes());
    }
}
