class while1 {

    public int given() {
        while (true) {
            System.out.println("Hello world");
        }
    }

    public void want() {
        while (true) {
            if (System.nanoTime() > 1L) throw new RuntimeException("timeout");
            System.out.println("Hello world");
        }
    }
}