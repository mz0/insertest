package insertest.structure;

import io.vertx.jdbcclient.JDBCConnectOptions;

import static java.util.Objects.requireNonNull;

public class ConnectOpts {
    private final String jdbcUrl;
    private final String username;
    private final String password;

    public ConnectOpts(String jdbcUrl, String dbUser, String dbPassword) {
        requireNonNull(jdbcUrl, "JDBC URL");
        requireNonNull(dbUser, "dbUsername");
        requireNonNull(dbPassword, "dbPassword");
        this.jdbcUrl = jdbcUrl; this.username = dbUser; this.password = dbPassword;
    }

    public JDBCConnectOptions asJDBCConnectOptions() {
        if (jdbcUrl == null) throw new IllegalStateException("attempt to use uninitialized ConnectOpts");
        return new JDBCConnectOptions().setJdbcUrl(this.jdbcUrl).setUser(this.username).setPassword(this.password);
    }

}
