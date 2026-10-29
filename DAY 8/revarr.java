import java.util.*;

class Main {
    static void reverse(int[] arr) {
        for (int i = arr.length - 1; i >= 0; i--)
            System.out.print(arr[i] + " ");
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();
        int[] arr = new int[n];

        for (int i = 0; i < n; i++)
            arr[i] = sc.nextInt();

        reverse(arr);
    }
}
