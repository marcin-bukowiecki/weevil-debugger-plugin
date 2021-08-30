class Assign3 {

    public void given(String s) {
        var a = 123;
        a = 100;
    }

    public void want(String s) {
        var a = java.lang.Integer.parseInt(applyFunction.apply(Arrays.asList(1, 123, "entry", 0, Thread.currentThread().getId())).toString());
        a = java.lang.Integer.parseInt(applyFunction.apply(Arrays.asList(1, 100, "entry", 0, Thread.currentThread().getId())).toString());
    }
}