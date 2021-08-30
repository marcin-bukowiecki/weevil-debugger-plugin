import java.util.HashMap;

class NewMap {

    public void given() {
        var result = new HashMap<String, Integer>();
    }

    public void want() {
        var result = (HashMap<String, Integer>) applyFunction.apply(Arrays.asList(0, new HashMap<String, Integer>(), "entry", 0, Thread.currentThread().getId()));
    }
}