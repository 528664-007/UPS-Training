import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class App {
    private static final String URL = "jdbc:mysql://localhost:3306/student_management?serverTimezone=UTC";
    private static final String USER = System.getenv().getOrDefault("DB_USER", "root");
    private static final String PASSWORD = System.getenv().getOrDefault("DB_PASSWORD", "");

    public static void main(String[] args) {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             Scanner scanner = new Scanner(System.in)) {
            System.out.println("Connected to the student management database.");

            boolean running = true;
            while (running) {
                printMenu();
                int choice = readInt(scanner, "Choose an option: ", 1, 6);

                try {
                    switch (choice) {
                        case 1:
                            addStudent(connection, scanner);
                            break;
                        case 2:
                            listStudents(connection);
                            break;
                        case 3:
                            findStudent(connection, scanner);
                            break;
                        case 4:
                            updateStudent(connection, scanner);
                            break;
                        case 5:
                            deleteStudent(connection, scanner);
                            break;
                        case 6:
                            running = false;
                            break;
                        default:
                            System.out.println("Invalid option.");
                    }
                } catch (SQLException e) {
                    System.out.println("Database operation failed: " + e.getMessage());
                }
            }
            System.out.println("Goodbye.");
        } catch (SQLException e) {
            System.err.println("Could not connect to MySQL: " + e.getMessage());
            System.err.println("Check the database setup and DB_USER / DB_PASSWORD settings.");
        }
    }

    private static void printMenu() {
        System.out.println("\n--- Student Management System ---");
        System.out.println("1. Add student");
        System.out.println("2. List students");
        System.out.println("3. Find student by ID");
        System.out.println("4. Update student");
        System.out.println("5. Delete student");
        System.out.println("6. Exit");
    }

    private static void addStudent(Connection connection, Scanner scanner) throws SQLException {
        String name = readRequiredText(scanner, "Student name: ");
        String email = readRequiredText(scanner, "Email: ");
        int age = readInt(scanner, "Age: ", 1, 120);
        String course = readRequiredText(scanner, "Course: ");

        String sql = "INSERT INTO students (name, email, age, course) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setString(2, email);
            statement.setInt(3, age);
            statement.setString(4, course);
            statement.executeUpdate();
        }
        System.out.println("Student added.");
    }

    private static void listStudents(Connection connection) throws SQLException {
        String sql = "SELECT id, name, email, age, course FROM students ORDER BY id";
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet results = statement.executeQuery()) {
            System.out.println("\nID | Name | Email | Age | Course");
            System.out.println("------------------------------------------------------------");
            boolean found = false;
            while (results.next()) {
                found = true;
                printStudent(results);
            }
            if (!found) {
                System.out.println("No students found.");
            }
        }
    }

    private static void findStudent(Connection connection, Scanner scanner) throws SQLException {
        int id = readInt(scanner, "Student ID: ", 1, Integer.MAX_VALUE);
        String sql = "SELECT id, name, email, age, course FROM students WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    printStudent(results);
                } else {
                    System.out.println("No student found with ID " + id + ".");
                }
            }
        }
    }

    private static void updateStudent(Connection connection, Scanner scanner) throws SQLException {
        int id = readInt(scanner, "Student ID to update: ", 1, Integer.MAX_VALUE);
        String name = readRequiredText(scanner, "New name: ");
        String email = readRequiredText(scanner, "New email: ");
        int age = readInt(scanner, "New age: ", 1, 120);
        String course = readRequiredText(scanner, "New course: ");

        String sql = "UPDATE students SET name = ?, email = ?, age = ?, course = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setString(2, email);
            statement.setInt(3, age);
            statement.setString(4, course);
            statement.setInt(5, id);
            int rows = statement.executeUpdate();
            System.out.println(rows == 0 ? "No student found with that ID." : "Student updated.");
        }
    }

    private static void deleteStudent(Connection connection, Scanner scanner) throws SQLException {
        int id = readInt(scanner, "Student ID to delete: ", 1, Integer.MAX_VALUE);
        String sql = "DELETE FROM students WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            int rows = statement.executeUpdate();
            System.out.println(rows == 0 ? "No student found with that ID." : "Student deleted.");
        }
    }

    private static void printStudent(ResultSet results) throws SQLException {
        System.out.printf("%d | %s | %s | %d | %s%n",
                results.getInt("id"), results.getString("name"), results.getString("email"),
                results.getInt("age"), results.getString("course"));
    }

    private static String readRequiredText(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            if (!value.isEmpty()) {
                return value;
            }
            System.out.println("This field cannot be empty.");
        }
    }

    private static int readInt(Scanner scanner, String prompt, int minimum, int maximum) {
        while (true) {
            System.out.print(prompt);
            try {
                int value = Integer.parseInt(scanner.nextLine().trim());
                if (value >= minimum && value <= maximum) {
                    return value;
                }
            } catch (NumberFormatException ignored) {
                // Prompt again when the input is not an integer.
            }
            System.out.printf("Enter a whole number between %d and %d.%n", minimum, maximum);
        }
    }
}
