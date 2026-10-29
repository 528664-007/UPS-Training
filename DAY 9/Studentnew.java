class Studentnew {
    int id;
    String name;

    Studentnew() {
        id = 0;
        name = "Unknown";
    }

    Studentnew(int i, String n) {
        id = i;
        name = n;
    }

    void display() {
        System.out.println(id + " " + name);
    }

    public static void main(String[] args) {
        Studentnew s1 = new Studentnew();
        Studentnew s2 = new Studentnew(101, "Sameer");

        s1.display();
        s2.display();
    }
}
