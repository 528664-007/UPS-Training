import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

public class App {
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final Scanner input = new Scanner(System.in);
    private final Map<String, Product> products = new LinkedHashMap<>();
    private final List<SaleLine> sales = new ArrayList<>();
    private final Path dataFile;

    private App(Path dataFile) {
        this.dataFile = dataFile;
    }

    public static void main(String[] args) {
        Path dataFile = args.length == 0
                ? Paths.get("data", "supermarket-data.csv")
                : Paths.get(args[0]);
        App app = new App(dataFile);
        try {
            app.load();
            app.run();
        } catch (IOException exception) {
            System.err.println("Could not read or save data at " + dataFile.toAbsolutePath());
            System.err.println(exception.getMessage());
            System.exit(1);
        }
    }

    private void run() throws IOException {
        System.out.println("Supermarket Manager");
        System.out.println("Data file: " + dataFile.toAbsolutePath());
        boolean running = true;
        while (running) {
            System.out.println("\n1. List products   2. Add product     3. Edit product");
            System.out.println("4. Remove product  5. Search          6. Restock product");
            System.out.println("7. Checkout        8. Low-stock list  9. Sales report");
            System.out.println("10. Exit");
            String choice = prompt("Choose an option: ");
            switch (choice) {
                case "1": listProducts(); break;
                case "2": addProduct(); break;
                case "3": editProduct(); break;
                case "4": removeProduct(); break;
                case "5": searchProducts(); break;
                case "6": restockProduct(); break;
                case "7": checkout(); break;
                case "8": showLowStock(); break;
                case "9": showSalesReport(); break;
                case "10": running = false; break;
                default: System.out.println("Choose a number from 1 to 10.");
            }
        }
        System.out.println("Goodbye.");
    }

    private void addProduct() throws IOException {
        String name = requiredText("Product name: ");
        String category = requiredText("Category: ");
        BigDecimal price = readPrice("Unit price: ");
        int quantity = readNonNegativeInt("Starting quantity: ");
        int threshold = readNonNegativeInt("Low-stock alert threshold: ");
        String id = nextProductId();
        products.put(id, new Product(id, name, category, price, quantity, threshold));
        save();
        System.out.println("Added " + name + " with ID " + id + ".");
    }

    private void editProduct() throws IOException {
        Product product = findProduct(prompt("Product ID to edit: "));
        if (product == null) return;

        System.out.println("Press Enter to keep the current value.");
        String name = optionalText("Name [" + product.name + "]: ", product.name);
        String category = optionalText("Category [" + product.category + "]: ", product.category);
        BigDecimal price = optionalPrice("Unit price [" + money(product.price) + "]: ", product.price);
        int quantity = optionalNonNegativeInt(
                "Quantity [" + product.quantity + "]: ", product.quantity);
        int threshold = optionalNonNegativeInt(
                "Low-stock threshold [" + product.lowStockThreshold + "]: ",
                product.lowStockThreshold);

        Product previous = product.copy();
        product.update(name, category, price, quantity, threshold);
        try {
            save();
            System.out.println("Product " + product.id + " updated.");
        } catch (IOException exception) {
            products.put(previous.id, previous);
            throw exception;
        }
    }

    private void removeProduct() throws IOException {
        Product product = findProduct(prompt("Product ID to remove: "));
        if (product == null) return;
        if (!yesNo("Remove " + product.name + " (" + product.id + ")? (y/n): ")) {
            System.out.println("Removal cancelled.");
            return;
        }
        products.remove(product.id);
        try {
            save();
            System.out.println("Product removed. Past sales records are retained.");
        } catch (IOException exception) {
            products.put(product.id, product);
            throw exception;
        }
    }

    private void restockProduct() throws IOException {
        Product product = findProduct(prompt("Product ID to restock: "));
        if (product == null) return;
        int amount = readPositiveInt("Quantity to add: ");
        if (amount > Integer.MAX_VALUE - product.quantity) {
            System.out.println("That quantity would exceed the supported stock limit.");
            return;
        }
        int previousQuantity = product.quantity;
        product.quantity += amount;
        try {
            save();
            System.out.println(product.name + " restocked. New quantity: " + product.quantity);
        } catch (IOException exception) {
            product.quantity = previousQuantity;
            throw exception;
        }
    }

    private void listProducts() {
        if (products.isEmpty()) {
            System.out.println("No products in inventory.");
            return;
        }
        printProductHeader();
        for (Product product : products.values()) printProduct(product);
    }

    private void searchProducts() {
        String query = prompt("Search by ID, name, or category: ")
                .toLowerCase(Locale.ROOT);
        boolean found = false;
        printProductHeader();
        for (Product product : products.values()) {
            if (product.id.toLowerCase(Locale.ROOT).contains(query)
                    || product.name.toLowerCase(Locale.ROOT).contains(query)
                    || product.category.toLowerCase(Locale.ROOT).contains(query)) {
                printProduct(product);
                found = true;
            }
        }
        if (!found) System.out.println("No matching products.");
    }

    private void showLowStock() {
        boolean found = false;
        printProductHeader();
        for (Product product : products.values()) {
            if (product.quantity <= product.lowStockThreshold) {
                printProduct(product);
                found = true;
            }
        }
        if (!found) System.out.println("No products are at or below their alert threshold.");
    }

    private void checkout() throws IOException {
        Map<String, Integer> cart = new LinkedHashMap<>();
        System.out.println("Enter product IDs and quantities. Leave ID blank to finish.");
        while (true) {
            String id = prompt("Product ID: ").trim().toUpperCase(Locale.ROOT);
            if (id.isEmpty()) break;
            Product product = products.get(id);
            if (product == null) {
                System.out.println("Product not found.");
                continue;
            }
            int alreadyAdded = cart.containsKey(id) ? cart.get(id) : 0;
            int quantity = readPositiveInt("Quantity (available: "
                    + (product.quantity - alreadyAdded) + "): ");
            if (quantity > product.quantity - alreadyAdded) {
                System.out.println("Not enough stock. Item was not added.");
                continue;
            }
            cart.put(id, alreadyAdded + quantity);
        }
        if (cart.isEmpty()) {
            System.out.println("Cart is empty.");
            return;
        }

        BigDecimal total = BigDecimal.ZERO;
        System.out.println("\nReceipt");
        System.out.println("--------------------------------------------------");
        for (Map.Entry<String, Integer> entry : cart.entrySet()) {
            Product product = products.get(entry.getKey());
            BigDecimal lineTotal = product.price.multiply(BigDecimal.valueOf(entry.getValue()));
            total = total.add(lineTotal);
            System.out.println(product.name + " x " + entry.getValue() + " @ "
                    + money(product.price) + " = " + money(lineTotal));
        }
        System.out.println("Total: " + money(total));
        if (!yesNo("Complete sale? (y/n): ")) {
            System.out.println("Checkout cancelled.");
            return;
        }

        String invoiceId = UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
        String date = LocalDateTime.now().format(DATE_FORMAT);
        Map<String, Integer> previousQuantities = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : cart.entrySet()) {
            Product product = products.get(entry.getKey());
            previousQuantities.put(product.id, product.quantity);
            product.quantity -= entry.getValue();
            sales.add(new SaleLine(invoiceId, product.id, product.name,
                    entry.getValue(), product.price, date));
        }
        try {
            save();
        } catch (IOException exception) {
            for (Map.Entry<String, Integer> entry : previousQuantities.entrySet()) {
                products.get(entry.getKey()).quantity = entry.getValue();
            }
            removeInvoice(invoiceId);
            throw exception;
        }
        System.out.println("Sale completed. Invoice: " + invoiceId);
    }

    private void showSalesReport() {
        if (sales.isEmpty()) {
            System.out.println("No sales have been recorded.");
            return;
        }
        Map<String, BigDecimal> invoiceTotals = new LinkedHashMap<>();
        Map<String, String> invoiceDates = new LinkedHashMap<>();
        BigDecimal revenue = BigDecimal.ZERO;
        for (SaleLine sale : sales) {
            BigDecimal lineTotal = sale.unitPrice.multiply(BigDecimal.valueOf(sale.quantity));
            revenue = revenue.add(lineTotal);
            BigDecimal invoiceTotal = invoiceTotals.containsKey(sale.invoiceId)
                    ? invoiceTotals.get(sale.invoiceId) : BigDecimal.ZERO;
            invoiceTotals.put(sale.invoiceId, invoiceTotal.add(lineTotal));
            invoiceDates.put(sale.invoiceId, sale.date);
        }
        System.out.println("\nSales");
        for (String invoiceId : invoiceTotals.keySet()) {
            System.out.println(invoiceDates.get(invoiceId) + "  Invoice " + invoiceId
                    + "  Total " + money(invoiceTotals.get(invoiceId)));
            for (SaleLine sale : sales) {
                if (sale.invoiceId.equals(invoiceId)) {
                    System.out.println("  " + sale.productName + " (" + sale.productId + ") x "
                            + sale.quantity + " @ " + money(sale.unitPrice));
                }
            }
        }
        System.out.println("Number of sales: " + invoiceTotals.size());
        System.out.println("Total revenue: " + money(revenue));
    }

    private void load() throws IOException {
        if (!Files.exists(dataFile)) return;
        int lineNumber = 0;
        try (BufferedReader reader = Files.newBufferedReader(dataFile, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (lineNumber == 1 && line.startsWith("type,")) continue;
                List<String> fields = parseCsv(line);
                if (fields.size() != 8) {
                    throw new IOException("Invalid CSV record at line " + lineNumber + ".");
                }
                try {
                    if ("PRODUCT".equals(fields.get(0))) {
                        Product product = new Product(fields.get(1), fields.get(2), fields.get(3),
                                new BigDecimal(fields.get(4)), Integer.parseInt(fields.get(5)),
                                Integer.parseInt(fields.get(6)));
                        if (products.put(product.id, product) != null) {
                            throw new IOException("Duplicate product ID at line " + lineNumber + ".");
                        }
                    } else if ("SALE".equals(fields.get(0))) {
                        sales.add(new SaleLine(fields.get(1), fields.get(2), fields.get(3),
                                Integer.parseInt(fields.get(5)), new BigDecimal(fields.get(4)),
                                fields.get(7)));
                    } else {
                        throw new IOException("Unknown record type at line " + lineNumber + ".");
                    }
                } catch (NumberFormatException | ArithmeticException exception) {
                    throw new IOException("Invalid numeric value at line " + lineNumber + ".", exception);
                }
            }
        } catch (NumberFormatException exception) {
            throw new IOException("Invalid data in " + dataFile + ".", exception);
        }
    }

    private void save() throws IOException {
        Path absoluteFile = dataFile.toAbsolutePath();
        Path parent = absoluteFile.getParent();
        Files.createDirectories(parent);
        Path temporaryFile = Files.createTempFile(parent, "supermarket-", ".tmp");
        boolean moved = false;
        try {
            try (BufferedWriter writer =
                         Files.newBufferedWriter(temporaryFile, StandardCharsets.UTF_8)) {
                writer.write("type,id,name,category,price,quantity,lowStockThreshold,date");
                writer.newLine();
                for (Product product : products.values()) {
                    writeCsvRow(writer, "PRODUCT", product.id, product.name, product.category,
                            product.price.toPlainString(), String.valueOf(product.quantity),
                            String.valueOf(product.lowStockThreshold), "");
                }
                for (SaleLine sale : sales) {
                    writeCsvRow(writer, "SALE", sale.invoiceId, sale.productName, sale.productId,
                            sale.unitPrice.toPlainString(), String.valueOf(sale.quantity), "", sale.date);
                }
            }
            try {
                Files.move(temporaryFile, absoluteFile, StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporaryFile, absoluteFile, StandardCopyOption.REPLACE_EXISTING);
            }
            moved = true;
        } finally {
            if (!moved) Files.deleteIfExists(temporaryFile);
        }
    }

    private static void writeCsvRow(BufferedWriter writer, String... fields) throws IOException {
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) writer.write(',');
            String field = fields[i];
            if (field.indexOf(',') >= 0 || field.indexOf('"') >= 0
                    || field.indexOf('\n') >= 0 || field.indexOf('\r') >= 0) {
                writer.write('"');
                writer.write(field.replace("\"", "\"\""));
                writer.write('"');
            } else {
                writer.write(field);
            }
        }
        writer.newLine();
    }

    private static List<String> parseCsv(String line) throws IOException {
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char character = line.charAt(i);
            if (quoted) {
                if (character == '"' && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    field.append('"');
                    i++;
                } else if (character == '"') {
                    quoted = false;
                } else {
                    field.append(character);
                }
            } else if (character == ',' ) {
                fields.add(field.toString());
                field.setLength(0);
            } else if (character == '"' && field.length() == 0) {
                quoted = true;
            } else {
                field.append(character);
            }
        }
        if (quoted) throw new IOException("Unclosed quoted CSV field.");
        fields.add(field.toString());
        return fields;
    }

    private Product findProduct(String id) {
        Product product = products.get(id.trim().toUpperCase(Locale.ROOT));
        if (product == null) System.out.println("Product not found.");
        return product;
    }

    private String nextProductId() {
        int next = 1;
        for (String id : products.keySet()) {
            if (id.matches("P\\d+")) {
                try {
                    next = Math.max(next, Integer.parseInt(id.substring(1)) + 1);
                } catch (NumberFormatException ignored) {
                    // IDs that exceed the integer range are retained but not used for sequencing.
                }
            }
        }
        String id;
        do {
            id = String.format(Locale.ROOT, "P%04d", next++);
        } while (products.containsKey(id));
        return id;
    }

    private void removeInvoice(String invoiceId) {
        for (int i = sales.size() - 1; i >= 0; i--) {
            if (sales.get(i).invoiceId.equals(invoiceId)) sales.remove(i);
        }
    }

    private static void printProductHeader() {
        System.out.printf("%-8s %-24s %-18s %10s %8s %10s%n",
                "ID", "NAME", "CATEGORY", "PRICE", "STOCK", "LOW ALERT");
        System.out.println("-------------------------------------------------------------------------------");
    }

    private static void printProduct(Product product) {
        System.out.printf("%-8s %-24s %-18s %10s %8d %10d%n",
                product.id, fit(product.name, 24), fit(product.category, 18),
                money(product.price), product.quantity, product.lowStockThreshold);
    }

    private static String fit(String value, int width) {
        return value.length() <= width ? value : value.substring(0, width - 3) + "...";
    }

    private static String money(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private String prompt(String message) {
        System.out.print(message);
        return input.nextLine().trim();
    }

    private String requiredText(String message) {
        while (true) {
            String value = prompt(message);
            if (!value.isEmpty()) return value;
            System.out.println("This field cannot be empty.");
        }
    }

    private String optionalText(String message, String currentValue) {
        String value = prompt(message);
        return value.isEmpty() ? currentValue : value;
    }

    private BigDecimal readPrice(String message) {
        while (true) {
            try {
                BigDecimal value = new BigDecimal(prompt(message)).setScale(2, RoundingMode.HALF_UP);
                if (value.signum() > 0) return value;
            } catch (NumberFormatException exception) {
                System.out.println("Enter a valid price greater than zero.");
                continue;
            }
            System.out.println("Enter a valid price greater than zero.");
        }
    }

    private BigDecimal optionalPrice(String message, BigDecimal currentValue) {
        while (true) {
            String value = prompt(message);
            if (value.isEmpty()) return currentValue;
            try {
                BigDecimal price = new BigDecimal(value).setScale(2, RoundingMode.HALF_UP);
                if (price.signum() > 0) return price;
            } catch (NumberFormatException exception) {
                System.out.println("Enter a valid price greater than zero.");
                continue;
            }
            System.out.println("Enter a valid price greater than zero.");
        }
    }

    private int readPositiveInt(String message) {
        while (true) {
            int value = readInteger(message);
            if (value > 0) return value;
            System.out.println("Enter a whole number greater than zero.");
        }
    }

    private int readNonNegativeInt(String message) {
        while (true) {
            int value = readInteger(message);
            if (value >= 0) return value;
            System.out.println("Enter zero or a positive whole number.");
        }
    }

    private int optionalNonNegativeInt(String message, int currentValue) {
        while (true) {
            String value = prompt(message);
            if (value.isEmpty()) return currentValue;
            try {
                int number = Integer.parseInt(value);
                if (number >= 0) return number;
            } catch (NumberFormatException exception) {
                System.out.println("Enter zero or a positive whole number.");
                continue;
            }
            System.out.println("Enter zero or a positive whole number.");
        }
    }

    private int readInteger(String message) {
        while (true) {
            try {
                return Integer.parseInt(prompt(message));
            } catch (NumberFormatException exception) {
                System.out.println("Enter a whole number.");
            }
        }
    }

    private boolean yesNo(String message) {
        while (true) {
            String answer = prompt(message).toLowerCase(Locale.ROOT);
            if ("y".equals(answer) || "yes".equals(answer)) return true;
            if ("n".equals(answer) || "no".equals(answer)) return false;
            System.out.println("Enter y or n.");
        }
    }

    private static final class Product {
        private final String id;
        private String name;
        private String category;
        private BigDecimal price;
        private int quantity;
        private int lowStockThreshold;

        private Product(String id, String name, String category, BigDecimal price,
                        int quantity, int lowStockThreshold) {
            this.id = id;
            update(name, category, price, quantity, lowStockThreshold);
        }

        private void update(String name, String category, BigDecimal price, int quantity,
                            int lowStockThreshold) {
            this.name = name;
            this.category = category;
            this.price = price;
            this.quantity = quantity;
            this.lowStockThreshold = lowStockThreshold;
        }

        private Product copy() {
            return new Product(id, name, category, price, quantity, lowStockThreshold);
        }
    }

    private static final class SaleLine {
        private final String invoiceId;
        private final String productId;
        private final String productName;
        private final int quantity;
        private final BigDecimal unitPrice;
        private final String date;

        private SaleLine(String invoiceId, String productId, String productName,
                         int quantity, BigDecimal unitPrice, String date) {
            this.invoiceId = invoiceId;
            this.productId = productId;
            this.productName = productName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.date = date;
        }
    }
}
