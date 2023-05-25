package insertest;

import com.google.gson.*;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class RandomJson {
    private HashMap<String, Object> jm;
    private RandomJson() {
    }

    public static String get() {
        RandomJson result = new RandomJson();
        String k = RandomStringUtils.randomAlphanumeric(6);
        result.init(k, randomList());
        Gson gson = new GsonBuilder().create();
        return gson.toJson(result.jm);
    }

    private void init(String k, Object v) {
        jm = new HashMap<>();
        jm.put(k, v);
    }

    private static List<Double> randomList() {
        return Arrays.asList(Math.random() * 10, Math.random() * 100, Math.random());
    }
}
