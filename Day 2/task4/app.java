import java.util.*;
class Main {
	public static void main(String[] args) {
		Scanner sc= new Scanner(System.in);
		System.out.println("Enter your name:");
		String name = sc.nextLine();
		System.out.println("Enter your age:");
		int age = sc.nextInt();
		sc.nextLine();
		System.out.println("Enter your location:");
		String loc = sc.nextLine();
		System.out.println("Enter your role:");
		String role = sc.nextLine();
		System.out.println("BIO DATA:\n"+"Name: "+name+"\nAge: "+age+"\nLocation: "+loc+"\nRole: "+role);
		sc.close();
	}
}
