class Parent {
    void display() {
        System.out.println("Parent class");
    }
}

class Child extends Parent {
    void show() {
        System.out.println("Child class");
    }

    public static void main(String[] args) {
        Child c = new Child();
        c.display();
        c.show();
    }
}
