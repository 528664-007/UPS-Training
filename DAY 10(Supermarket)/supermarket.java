import java.util.Scanner;

class Product {
    String name;
    double price;
    int quantity;

    Product(String name, double price, int quantity) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    double getTotal() {
        return price * quantity;
    }

    void display() {
        System.out.println(name + "\t" + price + "\t"
                + quantity + "\t" + getTotal());
    }
}

class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Enter number of products: ");
        int n = sc.nextInt();
        sc.nextLine();

        Product[] products = new Product[n];
        double total = 0;

        for (int i = 0; i < n; i++) {
            System.out.println("\nEnter product " + (i + 1) + " details:");

            System.out.print("Name: ");
            String name = sc.nextLine();

            System.out.print("Price: ");
            double price = sc.nextDouble();

            System.out.print("Quantity: ");
            int quantity = sc.nextInt();
            sc.nextLine();

            products[i] = new Product(name, price, quantity);
            total += products[i].getTotal();
        }

        double discount = total >= 1000 ? total * 0.10 : 0;
        double finalAmount = total - discount;

        System.out.println("\n========== SUPERMARKET BILL ==========");
        System.out.println("Product\tPrice\tQty\tTotal");

        for (Product p : products)
            p.display();

        System.out.println("--------------------------------------");
        System.out.printf("Subtotal: Rs. %.2f%n", total);
        System.out.printf("Discount: Rs. %.2f%n", discount);
        System.out.printf("Final Amount: Rs. %.2f%n", finalAmount);
        System.out.println("======================================");

        sc.close();
    }
}
