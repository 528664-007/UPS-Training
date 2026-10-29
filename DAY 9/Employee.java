class Employee {
    private int id;
    private String name;
    private double salary;

    void setDetails(int i, String n, double s) {
        id = i;
        name = n;
        salary = s;
    }

    void display() {
        System.out.println(id);
        System.out.println(name);
        System.out.println(salary);
    }

    public static void main(String[] args) {
        Employee e = new Employee();
        e.setDetails(101, "Sameer", 30000);
        e.display();
    }
}
