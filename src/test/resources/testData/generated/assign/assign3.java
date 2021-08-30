class Assign3 {

    public void given() {
        Integer a = 123;
    }

    public void want() {
        Integer a = (Integer) applyFunction.apply(Arrays.asList(0, 123, "entry", 0, Thread.currentThread().getId()));
    }
}