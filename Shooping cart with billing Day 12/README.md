## Getting Started

Welcome to the VS Code Java world. Here is a guideline to help you get started to write Java code in Visual Studio Code.

## Folder Structure

The workspace contains two folders by default, where:

- `src`: the folder to maintain sources
- `lib`: the folder to maintain dependencies

Meanwhile, the compiled output files will be generated in the `bin` folder by default.

> If you want to customize the folder structure, open `.vscode/settings.json` and update the related settings there.

## Run the supermarket manager

Compile and run the program from the workspace root:

```sh
javac --release 8 -d bin src/App.java
java -cp bin App
```

Choose a role from the opening menu:

- **Customer** can browse available products, shop from the catalog, enter a
  name for the bill, choose a discount and payment method, and receive or
  retrieve an invoice by its ID.
- **Seller** can manage inventory, create a bill for a named customer, view
  sales and invoice history, and update a saved invoice's customer name, item
  quantities, discount, and payment details. Invoice updates adjust inventory
  and recalculate totals.

Checkout creates an itemized invoice with a percentage discount, 1.2% tax
applied after the discount, payment method, amount tendered, and change. Cash
payments must cover the total due; card and mobile payments are recorded for
the exact total. Prices are stored as decimal currency values, and checkout
checks available stock before completing a sale.

All inventory and sales data is saved in `data/supermarket-data.csv`. The
directory and file are created automatically when the first change is saved.
The CSV is Excel-compatible and includes invoice-level totals, payment
details, customer names, and sale lines. Existing CSV files with the original
eight columns and earlier billing rows continue to load; older sales do not
have historical customer, discount, tax, or payment details.
You can optionally provide a different CSV path:

```sh
java -cp bin App "C:\store\data.csv"
```

The CSV file is written as an atomic snapshot where supported. Do not edit it
while the program is running. Keep a backup of this file to preserve your data.

## Dependency Management

The `JAVA PROJECTS` view allows you to manage your dependencies. More details can be found [here](https://github.com/microsoft/vscode-java-dependency#manage-dependencies).
