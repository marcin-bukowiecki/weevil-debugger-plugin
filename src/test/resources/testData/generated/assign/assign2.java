class Assign1 {

    public void given() {
        var a = 123;
    }

    public void want() {
        var a = java.lang.Integer.parseInt(applyFunction.apply(Arrays.asList(0, 123, "entry", 0, Thread.currentThread().getId())).toString());
    }
}