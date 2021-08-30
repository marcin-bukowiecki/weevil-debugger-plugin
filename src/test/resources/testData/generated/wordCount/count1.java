import java.util.HashMap;
import java.lang.String;

class Count1 {

    public void given(String words) {
        var result = new HashMap<String, Integer>();
        String[] split = words.split(",");

        for (String s : split) {
            if (result.containsKey(s)) {
                result.put(s, result.get(s) + 1);
            } else {
                result.put(s, 1);
            }
        }

        return result;
    }

    public void want(String words) {
        var result = (HashMap<String, Integer>) applyFunction.apply(Arrays.asList(0, new HashMap<String, Integer>(), "entry", 0, Thread.currentThread().getId()));
        String[] split = (String[]) applyFunction.apply(Arrays.asList(1, words.split(","), "entry", 0, Thread.currentThread().getId()));

        for (String s : (String[]) applyFunction.apply(Arrays.asList(4, split, "block1", 0, Thread.currentThread().getId()))) {
            applyFunction.apply(Arrays.asList(3, s, "block1", 0, Thread.currentThread().getId()));
            applyFunction.apply(Arrays.asList(5, "block1", "block1", 0, Thread.currentThread().getId()));
            if (System.nanoTime() > 1L) throw new RuntimeException("timeout");
            boolean $$$bool$$$1 = result.containsKey((String) applyFunction.apply(Arrays.asList(8, s, "block1", 0, Thread.currentThread().getId())));
            applyFunction.apply(Arrays.asList(6, $$$bool$$$1, "block1", 0, Thread.currentThread().getId()));
            if ($$$bool$$$1) {
                applyFunction.apply(Arrays.asList(9, "block2", "block2", 0, Thread.currentThread().getId()));
                result.put((String) applyFunction.apply(Arrays.asList(11, s, "block2", 0, Thread.currentThread().getId())), applyFunction.apply(Arrays.asList(12, result.get((String) applyFunction.apply(Arrays.asList(14, s, "block2", 0, Thread.currentThread().getId()))) + 1, "block2", 0, Thread.currentThread().getId())));
            } else {
                applyFunction.apply(Arrays.asList(15, "block3", "block3", 0, Thread.currentThread().getId()));
                result.put((String) applyFunction.apply(Arrays.asList(17, s, "block3", 0, Thread.currentThread().getId())), 1);
            }
        }

        applyFunction.apply(Arrays.asList(19, applyFunction.apply(Arrays.asList(18, result, "entry", 0, Thread.currentThread().getId())), "entry", 0, Thread.currentThread().getId()));
        throw returnException;
    }
}