public class Cmp1 {

    public static final char foo = 0;

    public static void given() {
        char ch = 0;
        if (ch == Cmp1.foo) {

        }
    }

    public static void want() {
        char ch = applyFunction.apply(Arrays.asList(0, 0, "entry", 0, Thread.currentThread().getId())).toString().charAt(0);
        boolean $$$bool$$$1 = (boolean) applyFunction.apply(Arrays.asList(2, applyFunction.apply(Arrays.asList(3, ch, "entry", 0, Thread.currentThread().getId())).toString().charAt(0) == applyFunction.apply(Arrays.asList(4, Cmp1.foo, "entry", 0, Thread.currentThread().getId())).toString().charAt(0), "entry", 0, Thread.currentThread().getId()));
        applyFunction.apply(Arrays.asList(1, $$$bool$$$1, "entry", 0, Thread.currentThread().getId()));
        if ($$$bool$$$1) {
            applyFunction.apply(Arrays.asList(5, "block1", "block1", 0, Thread.currentThread().getId()));

        }
    }
}