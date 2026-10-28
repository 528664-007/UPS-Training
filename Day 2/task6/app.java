// Online Java Compiler (Editor)
// Write and run Java online using this editor.
import java.util.*;
class Main {
	  public static void main(String[] args) {
        Scanner sc= new Scanner(System.in);
        System.out.println("Enter 1 for celsius to fahrenheit and 2 for fahrenheit to celsius:");
        int choice= sc.nextInt();
        if (choice==1){
            System.out.println("Enter temperature in celsius:");
            double c= sc.nextDouble();
            double f= (c*9/5)+32;
            System.out.println("Temperature in fahrenheit is: "+f);
        }
        else{
            System.out.println("Enter temperature in fahrenheit:");
            double f= sc.nextDouble();
            double c= (f-32)*5/9;
            System.out.println("Temperature in celsius is: "+c);
        }
    }
}
