class Assign3 {

    public void given(String s) {
        var a = 123;
        if ((a = 100) == 100) {

        }
    }

    public void want(String s) {
        var a = java.lang.Integer.parseInt(applyFunction.apply(Arrays.asList(3, 123, "entry", 0, Thread.currentThread().getId())).toString());
        boolean $$$bool$$$1 = (boolean) applyFunction.apply(Arrays.asList(2, (a = java.lang.Integer.parseInt(applyFunction.apply(Arrays.asList(3, 100, "entry", 0, Thread.currentThread().getId())).toString())) == 100, "entry", 0, Thread.currentThread().getId()));
        applyFunction.apply(Arrays.asList(1, $$$bool$$$1, "entry", 0, Thread.currentThread().getId()));
        if ($$$bool$$$1) {
            applyFunction.apply(Arrays.asList(4, "block1", "block1", 0, Thread.currentThread().getId()));

        }
    }
}