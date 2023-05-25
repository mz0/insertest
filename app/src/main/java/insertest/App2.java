package insertest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.rmi.RemoteException;
import java.sql.*;
import java.util.Properties;

public class App2 {
    public static void main(String[] args) throws IOException {
        Logger logger = LogManager.getLogger();
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        String validString = "{\"name\": \"Cap & Non Protected Amer Call \"}";
        System.out.println(compactJson(gson, validString));
        String schemaName, jdbcUrl, tableName, selectSql, createSql;
        Properties connectionProps = new Properties();

        try (FileInputStream fis = new FileInputStream("connection.mysql.properties")) {
            connectionProps.load(fis);
        }
        schemaName = connectionProps.getProperty("use.schema");
        jdbcUrl =  connectionProps.getProperty("use.url") + "/" + schemaName;
        tableName = connectionProps.getProperty("use.table");
        selectSql = String.format("select * from %s", tableName);

        for (Object k : connectionProps.keySet()) {
            if (((String) k).startsWith("use.")) {
                connectionProps.remove(k);
            }
        }
        logger.info("{} DB - table {}", dbType(jdbcUrl), tableName);
        String[] jsons = {
            "{\"customer_name\": \"John\", \"items\": { \"description\": \"milk\", \"quantity\": 4 } }",
            "{\"customer_name\": \"Susan\", \"items\": { \"description\": \"bread\", \"quantity\": 2 } }",
            "{\"customer_name\": \"Mark\", \"items\": { \"description\": \"bananas\", \"quantity\": 12 } }",
            "{\"customer_name\": \"Jane\", \"items\": { \"description\": \"cereal\", \"quantity\": 1 } }"
        };
        try (Connection conn = DriverManager.getConnection(jdbcUrl, connectionProps);
             Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)
        ) {
            createTableIfMissing(tableName, stmt, true, "JSON");
            // there are 2 ways to save JSON
            //  - canonical eay via ?::JSON and setObject() - www.enterprisedb.com/blog/processing-postgresql-json-jsonb-data-java
            //  - using CAST(? AS json) - github.com/pgjdbc/pgjdbc/issues/265
            String inSql = String.format("INSERT INTO %s VALUES (?, CAST(? AS json))", tableName);
            PreparedStatement ps = conn.prepareStatement(inSql);
            long pk = 0;
            for (String jsn : jsons) {
                pk++;
                ps.setLong(1, pk);
                ps.setObject(2, jsn);
                ps.executeUpdate();
            }

            ResultSet rs = stmt.executeQuery(selectSql);
            ResultSetMetaData rsmd = rs.getMetaData();
            if (rsmd.getColumnCount() < 2 || !rsmd.getColumnName(2).equals("joe")) {
                throw new RemoteException("either there less than 2 columns, or column 2 in not named 'joe'");
            }
            logger.info("column 2 typeName {}", rsmd.getColumnTypeName(2));
            String joeJson;
            while (rs.next()) {
                try (InputStream is = rs.getBinaryStream(2);
                     BufferedReader br = new BufferedReader(new InputStreamReader(is))
                ) {
                    joeJson = br.lines().reduce("", String::concat);
                    System.out.printf("length %d: %s%n", joeJson.length(), joeJson);
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void createTableIfMissing(String tableName, Statement stmt, boolean isMysql, String columnType) throws SQLException {
        char quote = isMysql ? '`' : '"';
        String createSql = String.format("CREATE TABLE IF NOT EXISTS %c%s%c (id BIGINT PRIMARY KEY, joe %s)", quote, tableName, quote, columnType);
        stmt.executeUpdate(createSql);
    }

    private static String dbType(String jdbcUrl) {
     // jdbc:mysql://[host1][:port],[host2][:port][,[host3][:port]]...[/[database]][?prop1=pVal1[&prop2=pVal2]...]
     // jdbc:mysql:loadbalance://[host1][:port],[host2][:port][,[host3][:port]]...[/[database]][?prop1=pVal1...]
     // mysqlx: | mysqlx+srv:
     // jdbc:mysql+srv: | jdbc:mysql+srv:loadbalance: | jdbc:mysql+srv:replication:
     // jdbc:oracle:thin:@//myoracle.db.server:1521/my_servicename
     // jdbc:oracle:thin:@myoracle.db.server:1521:my_sid
     // jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=myoracle.db.server)(PORT=1521))... using tnsnames.ora file
        String[] uspl = jdbcUrl.split(":");
        if (uspl.length < 2) {
            return uspl[0].startsWith("mysqlx") ? "mysqlx" : "unknown";
        } else {
            return uspl[1];
        }
    }

    private static String compactJson(Gson gson, String jsonSWithSpaces) {
        return gson.toJson(JsonParser.parseString(jsonSWithSpaces));
    }
}
