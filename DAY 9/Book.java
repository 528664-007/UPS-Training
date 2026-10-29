class Book {
    String title;
    String author;
    double price;

    void display() {
        System.out.println(title);
        System.out.println(author);
        System.out.println(price);
    }

    public static void main(String[] args) {
        Book b = new Book();
        b.title = "Java";
        b.author = "James";
        b.price = 500;
        b.display();
    }
}
