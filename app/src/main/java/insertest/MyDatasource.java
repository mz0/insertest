package insertest;

import com.mysql.cj.jdbc.MysqlDataSource;

import javax.sql.DataSource;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class MyDatasource {
    public static DataSource getMySQLDataSource() {
		Properties props = new Properties();

		MysqlDataSource mysqlDS = null;
		try (FileInputStream fis = new FileInputStream("db.properties")) {
			props.load(fis);
			mysqlDS = new MysqlDataSource();
			mysqlDS.setURL(props.getProperty("DB_URL"));
			mysqlDS.setUser(props.getProperty("DB_USER"));
			mysqlDS.setPassword(props.getProperty("DB_PASSWORD"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return mysqlDS;
	}
}
