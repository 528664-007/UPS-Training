
import java.util.*;

class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Enter a capital letter (A-Z): ");
        char ch = Character.toUpperCase(sc.next().charAt(0));

        System.out.print("Enter size: ");
        int l = sc.nextInt();

        if (ch < 'A' || ch > 'Z' || l < 3) {
            System.out.println("Enter a valid letter and size (minimum 3).");
            sc.close();
            return;
        }

        for (int i = 0; i < l; i++) {
            for (int j = 0; j < l; j++) {
                boolean star = false;

                if (ch == 'A')
                    star = i == 0 || i == l / 2 || j == 0 || j == l - 1;
                else if (ch == 'B')
                    star = j == 0 || i == 0 || i == l / 2 || i == l - 1 || j == l - 1;
                else if (ch == 'C')
                    star = i == 0 || i == l - 1 || j == 0;
                else if (ch == 'D')
                    star = j == 0 || i == 0 || i == l - 1 || j == l - 1;
                else if (ch == 'E')
                    star = j == 0 || i == 0 || i == l / 2 || i == l - 1;
                else if (ch == 'F')
                    star = j == 0 || i == 0 || i == l / 2;
                else if (ch == 'G')
                    star = i == 0 || i == l - 1 || j == 0 ||
                           (i == l / 2 && j >= l / 2) ||
                           (j == l - 1 && i >= l / 2);
                else if (ch == 'H')
                    star = j == 0 || j == l - 1 || i == l / 2;
                else if (ch == 'I')
                    star = i == 0 || i == l - 1 || j == l / 2;
                else if (ch == 'J')
                    star = i == 0 || j == l / 2 ||
                           (i == l - 1 && j <= l / 2) ||
                           (j == 0 && i == l - 1);
                else if (ch == 'K')
                    star = j == 0 || i + j == l - 1 || i == j;
                else if (ch == 'L')
                    star = j == 0 || i == l - 1;
                else if (ch == 'M')
                    star = j == 0 || j == l - 1 ||
                           (i == j && i <= l / 2) ||
                           (i + j == l - 1 && i <= l / 2);
                else if (ch == 'N')
                    star = j == 0 || j == l - 1 || i == j;
                else if (ch == 'O')
                    star = i == 0 || i == l - 1 || j == 0 || j == l - 1;
                else if (ch == 'P')
                    star = j == 0 || i == 0 || i == l / 2 ||
                           (j == l - 1 && i < l / 2);
                else if (ch == 'Q')
                    star = i == 0 || i == l - 1 || j == 0 || j == l - 1 ||
                           (i == j && i >= l / 2);
                else if (ch == 'R')
                    star = j == 0 || i == 0 || i == l / 2 ||
                           (j == l - 1 && i < l / 2) ||
                           (i == j && i >= l / 2);
                else if (ch == 'S')
                    star = i == 0 || i == l / 2 || i == l - 1 ||
                           (j == 0 && i < l / 2) ||
                           (j == l - 1 && i > l / 2);
                else if (ch == 'T')
                    star = i == 0 || j == l / 2;
                else if (ch == 'U')
                    star = j == 0 || j == l - 1 || i == l - 1;
                else if (ch == 'V')
                    star = (j == 0 || j == l - 1) && i < l - 1 ||
                           (i == j && i >= l / 2) ||
                           (i + j == l - 1 && i >= l / 2);
                else if (ch == 'W')
                    star = j == 0 || j == l - 1 ||
                           (i == j && i >= l / 2) ||
                           (i + j == l - 1 && i >= l / 2);
                else if (ch == 'X')
                    star = i == j || i + j == l - 1;
                else if (ch == 'Y')
                    star = (i == j && i <= l / 2) ||
                           (i + j == l - 1 && i <= l / 2) ||
                           (j == l / 2 && i > l / 2);
                else if (ch == 'Z')
                    star = i == 0 || i == l - 1 || i + j == l - 1;

                System.out.print(star ? "*" : " ");
            }
            System.out.println();
        }

        sc.close();
    }
}
