package inserts.pipeline;

import static java.util.Objects.requireNonNull;

public class ConnectOpts {
    public static final String MYSQL_DEFAULT_URL = "jdbc:mysql://127.0.0.1:3306/ushat"
            + "?createDatabaseIfNotExist=Yes&sslMode=DISABLED";
    public final String jdbcUrl;
    public final String username;
    public final String password;

    public ConnectOpts(String jdbcUrl, String dbUser, String dbPassword) {
        requireNonNull(jdbcUrl, "JDBC URL");
        requireNonNull(dbUser, "dbUsername");
        requireNonNull(dbPassword, "dbPassword");
        this.jdbcUrl = jdbcUrl; this.username = dbUser; this.password = dbPassword;
    }
}
