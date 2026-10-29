import java.util.*;

class Main {
    static boolean isPalindrome(int n) {
        if (n < 0)
            return false;

        int temp = n, rev = 0;

        while (n > 0) {
            rev = rev * 10 + n % 10;
            n /= 10;
        }

        return temp == rev;
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();

        if (isPalindrome(n))
            System.out.println("Palindrome");
        else
            System.out.println("Not Palindrome");
    }
}
