import java.util.Map;

public class Foo {

    public void test() {
        var foo = new HashMap<Integer, String>();
        applyFunction.apply(Arrays.asList(0, foo, "entry", 1, Thread.currentThread().getId()));
        var bar = new HashMap<Integer, String>();
        applyFunction.apply(Arrays.asList(1, bar, "entry", 1, Thread.currentThread().getId()));
    }
}


