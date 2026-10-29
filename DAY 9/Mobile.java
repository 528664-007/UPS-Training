class Mobile {
    String brand;
    int price;

    Mobile(String b, int p) {
        brand = b;
        price = p;
    }

    void display() {
        System.out.println(brand + " " + price);
    }

    public static void main(String[] args) {
        Mobile m = new Mobile("Samsung", 20000);
        m.display();
    }
}
