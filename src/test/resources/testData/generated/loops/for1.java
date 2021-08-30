class while1 {

    public int given() {
        for (;true;) {
            System.out.println("Hello world");
        }
    }

    public void want() {
        for (;true;) {
            if (System.nanoTime() > 1L) throw new RuntimeException("timeout");
            System.out.println("Hello world");
        }
    }
}