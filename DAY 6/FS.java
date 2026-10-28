
import java.util.Scanner;

class FibonacciSeries {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Enter number of terms: ");
        int n = sc.nextInt();

        int a = 0, b = 1;

        if (n <= 0) {
            System.out.println("Enter a positive number");
        } else {
            for (int i = 1; i <= n; i++) {
                System.out.print(a + " ");
                int c = a + b;
                a = b;
                b = c;
            }
        }

        sc.close();
    }
}
