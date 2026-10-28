import java.util.*;
class Main {
	 public static void main(String[] args) {
        Scanner sc= new Scanner(System.in);
        System.out.println("Enter 1 for square and 2 for rectangle");
        int choice= sc.nextInt();
        if (choice==1){
            System.out.println("Enter the side:");
            int side= sc.nextInt();
            System.out.println("Area of square is: "+(side*side));
        }
        else{
            System.out.println("Enter the length:");
            int length= sc.nextInt();
            System.out.println("Enter the width:");
            int width= sc.nextInt();
            System.out.println("Area of rectangle is: "+(length*width));
        }
    }
}
