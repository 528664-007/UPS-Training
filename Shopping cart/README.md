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

The interactive menu supports product listing, adding, editing and removal,
search by product ID/name/category, restocking, checkout, low-stock alerts,
and sales history/revenue reporting. Prices are stored as decimal currency
values and checkout checks available stock before completing a sale.

All inventory and sales data is saved in `data/supermarket-data.csv`. The
directory and file are created automatically when the first change is saved.
You can optionally provide a different CSV path:

```sh
java -cp bin App "C:\store\data.csv"
```

The CSV file is written as an atomic snapshot where supported. Do not edit it
while the program is running. Keep a backup of this file to preserve your data.

## Dependency Management

The `JAVA PROJECTS` view allows you to manage your dependencies. More details can be found [here](https://github.com/microsoft/vscode-java-dependency#manage-dependencies).
