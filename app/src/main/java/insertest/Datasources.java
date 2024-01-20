package insertest;

import com.mchange.v2.c3p0.DriverManagerDataSource;
import com.mysql.cj.jdbc.MysqlDataSource;
//import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sql.DataSource;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class Datasources {
	// https://dev.mysql.com/doc/connector-j/en/connector-j-reference-configuration-properties.html
	// https://jdbc.postgresql.org/documentation/use/#connection-parameters
	public static final String MYSQL_DEFAULT_URL = "jdbc:mysql://127.0.0.1:3306/ushat?createDatabaseIfNotExist=Yes&sslMode=DISABLED";
	Logger logger = LogManager.getLogger();
    public static DataSource getMySQLDataSource(Boolean dbReset) {
		Properties props = new Properties();
		DriverManagerDataSource ds;
		MysqlDataSource mysqlDS;
		Path propFile = Paths.get("db.properties");
		try (FileInputStream fis = new FileInputStream(propFile.toFile())) {
			props.load(fis);
			mysqlDS = new MysqlDataSource();
			mysqlDS.setURL(props.getProperty("db.url", "jdbc:mysql://localhost:3306/mysql"));
			mysqlDS.setUser(props.getProperty("db.username", "shsha"));
			mysqlDS.setPassword(props.getProperty("db.password", "999"));
		} catch (IOException e) {
			throw new RuntimeException("error reading " + propFile.toAbsolutePath());
		}
        return mysqlDS;
	}

	private void asNew(Connection connection, String databaseName) throws SQLException {
		logger.info("Dropping database \"{}\" (if exists)", databaseName);
		executeUpdate(connection, String.format("DROP DATABASE IF EXISTS %s", databaseName));
		createDatabase(connection, databaseName);
	}

	private void createDatabase(Connection connection, String databaseName) throws SQLException {
        logger.info("Creating new database \"{}\"", databaseName);
        executeUpdate(connection, String.format("CREATE DATABASE %s", databaseName));
    }

	private void executeUpdate(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }
}
