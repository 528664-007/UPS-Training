import java.util.Scanner;

public class divisibleby3 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Enter a number: ");
        int n = sc.nextInt();

        if (n % 3 == 0)
            System.out.println("Divisible by 3");
        else
            System.out.println("Not divisible by 3");

        sc.close();
    }
}
