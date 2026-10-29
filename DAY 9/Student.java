class Student {
    int rollNo;
    String name;
    double marks;

    Student(int r, String n, double m) {
        rollNo = r;
        name = n;
        marks = m;
    }

    void display() {
        System.out.println(rollNo + " " + name + " " + marks);
    }

    public static void main(String[] args) {
        Student s = new Student(1, "Sameer", 90);
        s.display();
    }
}
