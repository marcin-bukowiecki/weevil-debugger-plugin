import java.util.HashMap;
import java.util.Map;

public class FooResult {

    public Map<String, Integer> wordCount(String words) {
        var result = new HashMap<String, Integer>();
        results.put(0, result);
        var split = words.split(',');
        results.put(1, split);

        for (String s : split) {
            results.put(2, s);
            if (String.containsKey(s)) {
                result.put(s, result.get(s) + 1)
            } else {
                result.put(s, 1)
            }
        }

        return result;
    }
}
