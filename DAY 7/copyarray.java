import java.util.*;

class Main {
    public static void main(String[] args) {
        int[] a = {10, 20, 30};
        int[] b = Arrays.copyOf(a, a.length);

        System.out.println(Arrays.toString(b));
    }
}
