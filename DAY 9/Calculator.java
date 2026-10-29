class Calculator {
    int add(int a, int b) {
        return a + b;
    }

    int subtract(int a, int b) {
        return a - b;
    }

    int multiply(int a, int b) {
        return a * b;
    }

    double divide(int a, int b) {
        return (double) a / b;
    }

    public static void main(String[] args) {
        Calculator c = new Calculator();

        System.out.println(c.add(10, 5));
        System.out.println(c.subtract(10, 5));
        System.out.println(c.multiply(10, 5));
        System.out.println(c.divide(10, 5));
    }
}
