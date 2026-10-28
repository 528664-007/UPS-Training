// Online Java Compiler (Editor)
// Write and run Java online using this editor.
import java.util.*;
class Main {
	   public static void main(String[] args) {
        Scanner sc= new Scanner(System.in);
        System.out.println("Enter first number:");
        int a= sc.nextInt();
        System.out.println("Enter second number:");
        int b= sc.nextInt();
        System.out.println("Before swapping:");
        System.out.println("First number:"+ a + " Second number:"+ b);
        a=a+b;
        b=a-b;
        a=a-b;
        System.out.println("After swapping:");
        System.out.println("First number:"+ a + " Second number:"+ b);
        
    }
}
