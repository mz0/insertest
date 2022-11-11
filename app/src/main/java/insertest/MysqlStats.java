package insertest;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MysqlStats {
    public static final String mysqlRequest = "show global status";
    public static final String UpProcKey = "Uptime";
    public static final String UpStatKey = "Uptime_since_flush_status";
    private final Map<String, Object> stats;
    public MysqlStats(ResultSet rs) throws SQLException {
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

    public List<AbstractMap.SimpleEntry<String, Long>> diff(MysqlStats other, int threshold) {
        // compare Uptime and Uptime_since_flush_status, if different - throw some Incomparable Argument Error
        long dUptime = this.getLong(UpProcKey) - other.getLong(UpProcKey);
        long dUptimeCont = this.getLong(UpStatKey) - other.getLong(UpStatKey);
        if (dUptime != dUptimeCont) {
            throw new IllegalArgumentException(String.format("Uptime diff is %d, but Uptime_since_flush_status diff " +
                    "is %d, those stats are incomparable", dUptime, dUptimeCont));
        }
        return other.stats.entrySet().stream()
                .filter(e -> !((e.getValue() instanceof String) || e.getKey().equals(UpStatKey)))
                .map(e -> { String k = e.getKey();
                    return new AbstractMap.SimpleEntry<>(k, this.getLong(k) - other.getLong(k)); })
                .filter(e -> e.getValue() >= threshold)
                .sorted(Map.Entry.comparingByValue())
                .collect(Collectors.toList());
    }

    public String diffSTable(MysqlStats other) {
        List<AbstractMap.SimpleEntry<String, Long>> sDiff = diff(other, 1);
        long deltaUptime = sDiff.stream().filter(e -> e.getKey().equals(UpProcKey)).findAny().orElseThrow().getValue();
        String plusmn = "±";
        StringBuilder sb = new StringBuilder(String.format("stats diff (over %d%s0.5s interval):\n", deltaUptime, plusmn));
        DecimalFormat fmt999B = new DecimalFormat("###,###,###,###");
        for (AbstractMap.SimpleEntry<String, Long> kv : sDiff) {
            if (kv.getKey().equals((UpProcKey))) { continue; }
            sb.append(String.format("%15s %s\n", fmt999B.format(kv.getValue()), kv.getKey()));
        }
        return sb.toString();
    }

    private long getLong(String key) {
        return (long) this.stats.get(key);
    }
}
