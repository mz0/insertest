package insertest;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class DbStats {
    public static final String mysqlRequest = "show global status WHERE Value <> 0";
    private Map<String, Object> stats;
    public DbStats(ResultSet rs) throws SQLException {
        stats = new HashMap<>();
        String key, valStr;
        long val;
        while (rs.next()) {
            key = rs.getString(1);
            valStr = rs.getString(2);
            try {
                val = Long.parseLong(valStr);
                stats.put(key, val);
            } catch (NumberFormatException e) {
                stats.put(key, valStr);
            }
        }
    }

    public Map<String, Object> getStats() {
        return stats;
    }

    public Map<String, Object> diff(DbStats other) {
        // TODO compare Uptime and Uptime_since_flush_status, if different - throw some ComparisonError
        long dUptime;
        long dUptimeCont;
        throw new IllegalArgumentException("Uptime diff is %d, but Uptime_since_flush_status is %d, those stats are incomparable");
    }
}
