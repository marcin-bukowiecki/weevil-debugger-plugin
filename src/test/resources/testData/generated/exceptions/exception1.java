class exception1 {

    public int given() {
        try {
            return 10;
        } catch (RuntimeException ex) {
            return 123;
        }
    }

    public int want() {
        try {
            applyFunction.apply(Arrays.asList(0, "block1", "block1", 0, Thread.currentThread().getId()));
            applyFunction.apply(Arrays.asList(1, 10, "block1", 0, Thread.currentThread().getId()));
            throw returnException;
        } catch (RuntimeException ex) {
            if (ex == returnException) throw ex;
            return 123;
        }
    }
}