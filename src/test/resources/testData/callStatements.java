import java.util.HashMap;
import java.util.Map;

public class Foo {

    public Map<String, Integer> wordCount(String words) {
        var result = new HashMap<String, Integer>();
        var split = words.split(',');

        for (String s : split) {
            if (String.containsKey(s)) {
                result.put(s, result.get(s) + 1)
            } else {
                result.put(s, 1)
            }
        }

        return result;
    }
}