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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

public class App {
    private static final BigDecimal TAX_RATE = new BigDecimal("1.2");
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final Scanner input = new Scanner(System.in);
    private final Map<String, Product> products = new LinkedHashMap<>();
    private final List<SaleLine> sales = new ArrayList<>();
    private final Map<String, Invoice> invoices = new LinkedHashMap<>();
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
            System.out.println("\n1. Customer");
            System.out.println("2. Seller");
            System.out.println("3. Exit");
            String choice = prompt("Choose a role: ");
            switch (choice) {
                case "1": runCustomerMenu(); break;
                case "2": runSellerMenu(); break;
                case "3": running = false; break;
                default: System.out.println("Choose a number from 1 to 3.");
            }
        }
        System.out.println("Goodbye.");
    }

    private void runCustomerMenu() throws IOException {
        boolean shopping = true;
        while (shopping) {
            System.out.println("\nCustomer");
            System.out.println("1. Browse products  2. Shop and get a bill");
            System.out.println("3. View a bill      4. Back");
            String choice = prompt("Choose an option: ");
            switch (choice) {
                case "1": showCustomerProducts(); break;
                case "2":
                    showCustomerProducts();
                    checkout(requiredText("Customer name for the bill: "));
                    break;
                case "3":
                    printInvoice(prompt("Invoice ID: ").toUpperCase(Locale.ROOT));
                    break;
                case "4": shopping = false; break;
                default: System.out.println("Choose a number from 1 to 4.");
            }
        }
    }

    private void runSellerMenu() throws IOException {
        boolean selling = true;
        while (selling) {
            System.out.println("\nSeller");
            System.out.println("1. List products    2. Add product      3. Edit product");
            System.out.println("4. Remove product   5. Search           6. Restock product");
            System.out.println("7. Low-stock list   8. New sale/bill    9. Sales report");
            System.out.println("10. Invoice history 11. Update invoice  12. Back");
            String choice = prompt("Choose an option: ");
            switch (choice) {
                case "1": listProducts(); break;
                case "2": addProduct(); break;
                case "3": editProduct(); break;
                case "4": removeProduct(); break;
                case "5": searchProducts(); break;
                case "6": restockProduct(); break;
                case "7": showLowStock(); break;
                case "8": checkout(requiredText("Customer name for the bill: ")); break;
                case "9": showSalesReport(); break;
                case "10": showInvoiceHistory(); break;
                case "11": updateInvoice(); break;
                case "12": selling = false; break;
                default: System.out.println("Choose a number from 1 to 12.");
            }
        }
    }

    private void showCustomerProducts() {
        boolean found = false;
        printProductHeader();
        for (Product product : products.values()) {
            if (product.quantity > 0) {
                printProduct(product);
                found = true;
            }
        }
        if (!found) System.out.println("No products are currently available.");
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

    private void checkout(String customerName) throws IOException {
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

        BigDecimal subtotal = BigDecimal.ZERO;
        for (Map.Entry<String, Integer> entry : cart.entrySet()) {
            Product product = products.get(entry.getKey());
            BigDecimal lineTotal = product.price.multiply(BigDecimal.valueOf(entry.getValue()));
            subtotal = subtotal.add(lineTotal);
        }
        BigDecimal discountPercent = readPercentage("Discount percentage (0-100): ");
        BigDecimal discountAmount = percentageOf(subtotal, discountPercent);
        BigDecimal taxableAmount = subtotal.subtract(discountAmount);
        BigDecimal taxAmount = percentageOf(taxableAmount, TAX_RATE);
        BigDecimal total = taxableAmount.add(taxAmount);
        String paymentMethod = readPaymentMethod();
        BigDecimal amountTendered = "CASH".equals(paymentMethod)
                ? readAmountAtLeast("Amount tendered (due " + money(total) + "): ", total)
                : total;
        BigDecimal change = amountTendered.subtract(total);

        System.out.println("\nInvoice preview");
        System.out.println("--------------------------------------------------");
        for (Map.Entry<String, Integer> entry : cart.entrySet()) {
            Product product = products.get(entry.getKey());
            BigDecimal lineTotal = product.price.multiply(BigDecimal.valueOf(entry.getValue()));
            System.out.println(product.name + " x " + entry.getValue() + " @ "
                    + money(product.price) + " = " + money(lineTotal));
        }
        System.out.println("Subtotal: " + money(subtotal));
        System.out.println("Discount (" + percentage(discountPercent) + "%): -"
                + money(discountAmount));
        System.out.println("Tax (" + percentage(TAX_RATE) + "%): " + money(taxAmount));
        System.out.println("Total due: " + money(total));
        System.out.println("Payment: " + paymentMethod + "  Tendered: "
                + money(amountTendered) + "  Change: " + money(change));
        if (!yesNo("Complete sale? (y/n): ")) {
            System.out.println("Checkout cancelled.");
            return;
        }

        String invoiceId = UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
        String date = LocalDateTime.now().format(DATE_FORMAT);
        Invoice invoice = new Invoice(invoiceId, customerName, date, subtotal, discountPercent,
                discountAmount, TAX_RATE, taxAmount, total, paymentMethod,
                amountTendered, change);
        Map<String, Integer> previousQuantities = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : cart.entrySet()) {
            Product product = products.get(entry.getKey());
            previousQuantities.put(product.id, product.quantity);
            product.quantity -= entry.getValue();
            sales.add(new SaleLine(invoiceId, product.id, product.name,
                    entry.getValue(), product.price, date));
        }
        invoices.put(invoiceId, invoice);
        try {
            save();
        } catch (IOException exception) {
            for (Map.Entry<String, Integer> entry : previousQuantities.entrySet()) {
                products.get(entry.getKey()).quantity = entry.getValue();
            }
            removeInvoice(invoiceId);
            throw exception;
        }
        System.out.println("Sale completed.");
        printInvoice(invoiceId);
    }

    private void showSalesReport() {
        if (sales.isEmpty()) {
            System.out.println("No sales have been recorded.");
            return;
        }
        Map<String, BigDecimal> invoiceSubtotals = new LinkedHashMap<>();
        Map<String, String> invoiceDates = new LinkedHashMap<>();
        BigDecimal revenue = BigDecimal.ZERO;
        for (SaleLine sale : sales) {
            BigDecimal lineTotal = sale.unitPrice.multiply(BigDecimal.valueOf(sale.quantity));
            BigDecimal invoiceSubtotal = invoiceSubtotals.containsKey(sale.invoiceId)
                    ? invoiceSubtotals.get(sale.invoiceId) : BigDecimal.ZERO;
            invoiceSubtotals.put(sale.invoiceId, invoiceSubtotal.add(lineTotal));
            invoiceDates.put(sale.invoiceId, sale.date);
        }
        System.out.println("\nSales");
        for (String invoiceId : invoiceSubtotals.keySet()) {
            Invoice invoice = invoices.get(invoiceId);
            BigDecimal invoiceTotal = invoice == null
                    ? invoiceSubtotals.get(invoiceId) : invoice.total;
            revenue = revenue.add(invoiceTotal);
            System.out.println(invoiceDates.get(invoiceId) + "  Invoice " + invoiceId
                    + "  Total " + money(invoiceTotal));
            for (SaleLine sale : sales) {
                if (sale.invoiceId.equals(invoiceId)) {
                    System.out.println("  " + sale.productName + " (" + sale.productId + ") x "
                            + sale.quantity + " @ " + money(sale.unitPrice));
                }
            }
        }
        System.out.println("Number of sales: " + invoiceSubtotals.size());
        System.out.println("Total revenue: " + money(revenue));
    }

    private void showInvoiceHistory() {
        if (sales.isEmpty()) {
            System.out.println("No invoices have been recorded.");
            return;
        }
        LinkedHashSet<String> invoiceIds = new LinkedHashSet<>();
        for (SaleLine sale : sales) invoiceIds.add(sale.invoiceId);
        System.out.println("\nInvoice history");
        for (String invoiceId : invoiceIds) {
            Invoice invoice = invoices.get(invoiceId);
            String date = invoice == null ? findInvoiceDate(invoiceId) : invoice.date;
            BigDecimal total = invoice == null
                    ? subtotalForInvoice(invoiceId) : invoice.total;
            System.out.println(date + "  " + invoiceId + "  Total " + money(total));
        }
        String invoiceId = prompt("Invoice ID to display (Enter to return): ")
                .toUpperCase(Locale.ROOT);
        if (!invoiceId.isEmpty()) printInvoice(invoiceId);
    }

    private void updateInvoice() throws IOException {
        String invoiceId = prompt("Invoice ID to update: ").toUpperCase(Locale.ROOT);
        List<SaleLine> previousLines = new ArrayList<>();
        for (SaleLine sale : sales) {
            if (sale.invoiceId.equals(invoiceId)) previousLines.add(sale);
        }
        if (previousLines.isEmpty()) {
            System.out.println("Invoice not found.");
            return;
        }

        Invoice previousInvoice = invoices.get(invoiceId);
        printInvoice(invoiceId);
        String customerName = previousInvoice == null ? ""
                : previousInvoice.customerName;
        customerName = optionalText("Customer name [" + customerName + "] (Enter to keep): ",
                customerName);
        if (customerName.isEmpty()) customerName = requiredText("Customer name for the bill: ");

        List<SaleLine> updatedLines = new ArrayList<>();
        System.out.println("Enter the new quantity for each current item (0 removes it).");
        for (SaleLine sale : previousLines) {
            int quantity = optionalNonNegativeInt(
                    sale.productName + " (" + sale.productId + ") quantity ["
                            + sale.quantity + "]: ", sale.quantity);
            if (quantity > 0) {
                updatedLines.add(new SaleLine(invoiceId, sale.productId, sale.productName,
                        quantity, sale.unitPrice, sale.date));
            }
        }
        System.out.println("Add items by product ID. Leave ID blank to finish.");
        while (true) {
            String productId = prompt("Product ID to add: ").toUpperCase(Locale.ROOT);
            if (productId.isEmpty()) break;
            Product product = products.get(productId);
            if (product == null) {
                System.out.println("Product not found.");
                continue;
            }
            int quantity = readPositiveInt("Quantity to add: ");
            updatedLines.add(new SaleLine(invoiceId, product.id, product.name, quantity,
                    product.price, previousLines.get(0).date));
        }
        if (updatedLines.isEmpty()) {
            System.out.println("An invoice must contain at least one item. Update cancelled.");
            return;
        }

        Map<String, Long> previousQuantities = quantityByProduct(previousLines);
        Map<String, Long> updatedQuantities = quantityByProduct(updatedLines);
        for (Map.Entry<String, Long> entry : updatedQuantities.entrySet()) {
            Product product = products.get(entry.getKey());
            if (product != null) {
                long availableForInvoice = (long) product.quantity
                        + previousQuantities.getOrDefault(entry.getKey(), 0L);
                if (entry.getValue() > availableForInvoice) {
                    System.out.println("Not enough stock for " + product.name
                            + ". Update cancelled.");
                    return;
                }
                long resultingStock = availableForInvoice - entry.getValue();
                if (resultingStock > Integer.MAX_VALUE) {
                    System.out.println("Updated quantity would exceed the stock limit for "
                            + product.name + ". Update cancelled.");
                    return;
                }
            }
        }
        for (String productId : previousQuantities.keySet()) {
            if (updatedQuantities.containsKey(productId)) continue;
            Product product = products.get(productId);
            if (product != null
                    && (long) product.quantity + previousQuantities.get(productId)
                            > Integer.MAX_VALUE) {
                System.out.println("Restoring this invoice would exceed the stock limit for "
                        + product.name + ". Update cancelled.");
                return;
            }
        }

        BigDecimal subtotal = subtotalOf(updatedLines);
        BigDecimal discountPercent = readPercentage("New discount percentage (0-100): ");
        BigDecimal discountAmount = percentageOf(subtotal, discountPercent);
        BigDecimal taxRate = previousInvoice == null ? TAX_RATE : previousInvoice.taxRate;
        BigDecimal taxAmount = percentageOf(subtotal.subtract(discountAmount), taxRate);
        BigDecimal total = subtotal.subtract(discountAmount).add(taxAmount);
        String paymentMethod = readPaymentMethod();
        BigDecimal amountTendered = "CASH".equals(paymentMethod)
                ? readAmountAtLeast("Amount tendered (due " + money(total) + "): ", total)
                : total;
        BigDecimal change = amountTendered.subtract(total);
        System.out.println("Updated subtotal: " + money(subtotal)
                + "  Discount: " + money(discountAmount)
                + "  Tax: " + money(taxAmount)
                + "  Total: " + money(total));
        if (!yesNo("Save invoice update? (y/n): ")) {
            System.out.println("Invoice update cancelled.");
            return;
        }

        Map<String, Integer> originalStock = new LinkedHashMap<>();
        for (Product product : products.values()) originalStock.put(product.id, product.quantity);
        LinkedHashSet<String> affectedProductIds =
                new LinkedHashSet<>(previousQuantities.keySet());
        affectedProductIds.addAll(updatedQuantities.keySet());
        for (String productId : affectedProductIds) {
            Product product = products.get(productId);
            if (product != null) {
                long oldQuantity = previousQuantities.containsKey(productId)
                        ? previousQuantities.get(productId) : 0L;
                long newQuantity = updatedQuantities.containsKey(productId)
                        ? updatedQuantities.get(productId) : 0L;
                product.quantity = (int) (product.quantity + oldQuantity - newQuantity);
            }
        }
        removeInvoice(invoiceId);
        sales.addAll(updatedLines);
        invoices.put(invoiceId, new Invoice(invoiceId, customerName,
                previousLines.get(0).date, subtotal, discountPercent, discountAmount,
                taxRate, taxAmount, total, paymentMethod, amountTendered, change));
        try {
            save();
        } catch (IOException exception) {
            for (Map.Entry<String, Integer> entry : originalStock.entrySet()) {
                products.get(entry.getKey()).quantity = entry.getValue();
            }
            removeInvoice(invoiceId);
            sales.addAll(previousLines);
            if (previousInvoice != null) invoices.put(invoiceId, previousInvoice);
            throw exception;
        }
        System.out.println("Invoice updated.");
        printInvoice(invoiceId);
    }

    private Map<String, Long> quantityByProduct(List<SaleLine> lines) {
        Map<String, Long> quantities = new LinkedHashMap<>();
        for (SaleLine sale : lines) {
            long existing = quantities.containsKey(sale.productId)
                    ? quantities.get(sale.productId) : 0L;
            quantities.put(sale.productId, existing + sale.quantity);
        }
        return quantities;
    }

    private BigDecimal subtotalOf(List<SaleLine> lines) {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (SaleLine sale : lines) {
            subtotal = subtotal.add(sale.unitPrice.multiply(BigDecimal.valueOf(sale.quantity)));
        }
        return subtotal;
    }

    private void printInvoice(String invoiceId) {
        List<SaleLine> invoiceSales = new ArrayList<>();
        for (SaleLine sale : sales) {
            if (sale.invoiceId.equals(invoiceId)) invoiceSales.add(sale);
        }
        if (invoiceSales.isEmpty()) {
            System.out.println("Invoice not found.");
            return;
        }
        Invoice invoice = invoices.get(invoiceId);
        BigDecimal subtotal = invoice == null
                ? subtotalForInvoice(invoiceId) : invoice.subtotal;
        BigDecimal discountPercent = invoice == null
                ? BigDecimal.ZERO : invoice.discountPercent;
        BigDecimal discountAmount = invoice == null
                ? BigDecimal.ZERO : invoice.discountAmount;
        BigDecimal taxRate = invoice == null ? BigDecimal.ZERO : invoice.taxRate;
        BigDecimal taxAmount = invoice == null ? BigDecimal.ZERO : invoice.taxAmount;
        BigDecimal total = invoice == null ? subtotal : invoice.total;

        System.out.println("\nInvoice " + invoiceId);
        if (invoice != null && !invoice.customerName.isEmpty()) {
            System.out.println("Customer: " + invoice.customerName);
        }
        System.out.println(invoice == null ? findInvoiceDate(invoiceId) : invoice.date);
        System.out.println("--------------------------------------------------");
        for (SaleLine sale : invoiceSales) {
            BigDecimal lineTotal = sale.unitPrice.multiply(BigDecimal.valueOf(sale.quantity));
            System.out.println(sale.productName + " (" + sale.productId + ") x "
                    + sale.quantity + " @ " + money(sale.unitPrice) + " = "
                    + money(lineTotal));
        }
        System.out.println("Subtotal: " + money(subtotal));
        System.out.println("Discount (" + percentage(discountPercent) + "%): -"
                + money(discountAmount));
        System.out.println("Tax (" + percentage(taxRate) + "%): " + money(taxAmount));
        System.out.println("Total: " + money(total));
        if (invoice == null) {
            System.out.println("Payment details unavailable for this older sale.");
        } else {
            System.out.println("Payment method: " + invoice.paymentMethod);
            System.out.println("Amount tendered: " + money(invoice.amountTendered));
            System.out.println("Change: " + money(invoice.change));
        }
        System.out.println("--------------------------------------------------");
    }

    private BigDecimal subtotalForInvoice(String invoiceId) {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (SaleLine sale : sales) {
            if (sale.invoiceId.equals(invoiceId)) {
                subtotal = subtotal.add(sale.unitPrice.multiply(
                        BigDecimal.valueOf(sale.quantity)));
            }
        }
        return subtotal;
    }

    private String findInvoiceDate(String invoiceId) {
        for (SaleLine sale : sales) {
            if (sale.invoiceId.equals(invoiceId)) return sale.date;
        }
        return "";
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
                if (fields.size() != 8 && fields.size() != 17 && fields.size() != 18) {
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
                        sales.add(new SaleLine(fields.get(1), fields.get(3), fields.get(2),
                                Integer.parseInt(fields.get(5)), new BigDecimal(fields.get(4)),
                                fields.get(7)));
                    } else if ("BILL".equals(fields.get(0))
                            && (fields.size() == 17 || fields.size() == 18)) {
                        Invoice invoice = new Invoice(fields.get(1),
                                fields.size() == 18 ? fields.get(17) : "", fields.get(7),
                                new BigDecimal(fields.get(8)), new BigDecimal(fields.get(9)),
                                new BigDecimal(fields.get(10)), new BigDecimal(fields.get(11)),
                                new BigDecimal(fields.get(12)), new BigDecimal(fields.get(13)),
                                fields.get(14), new BigDecimal(fields.get(15)),
                                new BigDecimal(fields.get(16)));
                        if (invoices.put(invoice.id, invoice) != null) {
                            throw new IOException("Duplicate invoice ID at line " + lineNumber + ".");
                        }
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
                writer.write("type,id,name,category,price,quantity,lowStockThreshold,date,"
                        + "subtotal,discountPercent,discountAmount,taxRate,taxAmount,total,"
                        + "paymentMethod,amountTendered,change,customerName");
                writer.newLine();
                for (Product product : products.values()) {
                    writeCsvRow(writer, "PRODUCT", product.id, product.name, product.category,
                            product.price.toPlainString(), String.valueOf(product.quantity),
                            String.valueOf(product.lowStockThreshold), "");
                }
                for (Invoice invoice : invoices.values()) {
                    writeCsvRow(writer, "BILL", invoice.id, "", "", "", "", "", invoice.date,
                            invoice.subtotal.toPlainString(),
                            invoice.discountPercent.toPlainString(),
                            invoice.discountAmount.toPlainString(), invoice.taxRate.toPlainString(),
                            invoice.taxAmount.toPlainString(), invoice.total.toPlainString(),
                            invoice.paymentMethod, invoice.amountTendered.toPlainString(),
                            invoice.change.toPlainString(), invoice.customerName);
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
        invoices.remove(invoiceId);
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

    private static BigDecimal percentageOf(BigDecimal amount, BigDecimal percentage) {
        return amount.multiply(percentage).divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);
    }

    private static String percentage(BigDecimal value) {
        return value.stripTrailingZeros().toPlainString();
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

    private BigDecimal readPercentage(String message) {
        while (true) {
            try {
                BigDecimal value = new BigDecimal(prompt(message));
                if (value.signum() >= 0 && value.compareTo(ONE_HUNDRED) <= 0) {
                    return value.setScale(2, RoundingMode.HALF_UP);
                }
            } catch (NumberFormatException exception) {
                System.out.println("Enter a percentage from 0 to 100.");
                continue;
            }
            System.out.println("Enter a percentage from 0 to 100.");
        }
    }

    private String readPaymentMethod() {
        while (true) {
            String method = prompt("Payment method (cash/card/mobile): ")
                    .toUpperCase(Locale.ROOT);
            if ("CASH".equals(method) || "CARD".equals(method) || "MOBILE".equals(method)) {
                return method;
            }
            System.out.println("Choose cash, card, or mobile.");
        }
    }

    private BigDecimal readAmountAtLeast(String message, BigDecimal minimum) {
        while (true) {
            try {
                BigDecimal amount = new BigDecimal(prompt(message))
                        .setScale(2, RoundingMode.HALF_UP);
                if (amount.compareTo(minimum) >= 0) return amount;
            } catch (NumberFormatException exception) {
                System.out.println("Enter a valid amount at least equal to the total due.");
                continue;
            }
            System.out.println("Amount tendered cannot be less than the total due.");
        }
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

    private static final class Invoice {
        private final String id;
        private final String customerName;
        private final String date;
        private final BigDecimal subtotal;
        private final BigDecimal discountPercent;
        private final BigDecimal discountAmount;
        private final BigDecimal taxRate;
        private final BigDecimal taxAmount;
        private final BigDecimal total;
        private final String paymentMethod;
        private final BigDecimal amountTendered;
        private final BigDecimal change;

        private Invoice(String id, String customerName, String date, BigDecimal subtotal,
                        BigDecimal discountPercent, BigDecimal discountAmount,
                        BigDecimal taxRate, BigDecimal taxAmount, BigDecimal total,
                        String paymentMethod, BigDecimal amountTendered, BigDecimal change) {
            this.id = id;
            this.customerName = customerName;
            this.date = date;
            this.subtotal = subtotal;
            this.discountPercent = discountPercent;
            this.discountAmount = discountAmount;
            this.taxRate = taxRate;
            this.taxAmount = taxAmount;
            this.total = total;
            this.paymentMethod = paymentMethod;
            this.amountTendered = amountTendered;
            this.change = change;
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
