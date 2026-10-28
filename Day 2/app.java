import java.util.*;
class Main {
     public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter first number: ");
        int n1 = sc.nextInt();
        System.out.print("Enter second number: ");
        int n2 = sc.nextInt();
        System.out.println("Addition:"+(n1+n2));
        System.out.println("Subtraction:"+(n1-n2));
        System.out.println("Multiplication:"+(n1*n2));
        System.out.println("Division:"+(n1/n2));
        String extra= sc.nextLine();
        System.out.println("Enter your name:");
        String name= sc.nextLine();
        System.out.println("My name is " + name);
    }
}
