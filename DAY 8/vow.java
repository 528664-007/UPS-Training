import java.util.*;

class Main {
    static int countVowels(String s) {
        int count = 0;
        s = s.toLowerCase();

        for (int i = 0; i < s.length(); i++)
            if ("aeiou".indexOf(s.charAt(i)) != -1)
                count++;

        return count;
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String s = sc.nextLine();
        System.out.println(countVowels(s));
    }
}
