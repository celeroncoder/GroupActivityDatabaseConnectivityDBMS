# Dynamic Database Query GUI

This JavaFX application provides a flexible interface for executing SQL queries on a database and displaying the results dynamically. It's designed to handle various types of SQL queries and adapt the display based on the result set.

## Features

- Dynamic query input
- Adaptive result display
- Real-time result filtering
- Support for various SQL query types

## How It Works

### 1. User Interface

The application presents a simple GUI with the following components:

- A text field for entering SQL queries
- An "Execute Query" button
- A text field for filtering results
- A table view for displaying query results

### 2. Query Execution

When the user enters a query and clicks "Execute Query", the following process occurs:

1. The application establishes a connection to the database.
2. It executes the user-provided SQL query.
3. The result set's metadata is analyzed to determine the structure of the results.

### 3. Dynamic Table Creation

The table view is created dynamically based on the query results:

1. Existing columns are cleared.
2. New columns are created based on the result set metadata.
3. Each column is configured with a cell value factory that can handle any data type.

```java
for (int i = 1; i <= columnCount; i++) {
    final int j = i - 1;
    TableColumn<ObservableList<String>, String> col = new TableColumn<>(metaData.getColumnName(i));
    col.setCellValueFactory(param -> {
        ObservableList<String> row = param.getValue();
        return new SimpleStringProperty(row.size() > j ? row.get(j) : "");
    });
    tableView.getColumns().add(col);
}
```

This approach allows the table to adapt to any query result structure.

### 4. Populating Data

The query results are stored in an `ObservableList<ObservableList<String>>`:

```java
ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
while (rs.next()) {
    ObservableList<String> row = FXCollections.observableArrayList();
    for (int i = 1; i <= columnCount; i++) {
        row.add(rs.getString(i));
    }
    data.add(row);
}
```

This nested structure allows for rows with varying numbers of columns.

### 5. Filtering Mechanism

The application implements real-time filtering of results:

1. A `FilteredList` is created from the master data.
2. A listener on the filter text field updates the predicate of the `FilteredList`.
3. The predicate checks if any cell in a row contains the filter text (case-insensitive).

```java
FilteredList<ObservableList<String>> filteredData = new FilteredList<>(masterData, p -> true);
filterField.textProperty().addListener((observable, oldValue, newValue) -> {
    filteredData.setPredicate(row -> {
        if (newValue == null || newValue.isEmpty()) {
            return true;
        }
        String lowerCaseFilter = newValue.toLowerCase();
        return row.stream().anyMatch(cell ->
            cell.toLowerCase().contains(lowerCaseFilter)
        );
    });
});
```

## Modularity of Query Handling

The key to the application's flexibility is its modular approach to query handling:

1. **Query Input**: The user can input any valid SQL query. The application doesn't make assumptions about the query structure.

2. **Dynamic Result Processing**: The application uses the result set's metadata to determine the structure of the results at runtime. This allows it to handle SELECT queries with any number of columns or complex JOINS.

3. **Flexible Data Storage**: By using `ObservableList<ObservableList<String>>`, the application can store and display results from queries that return different numbers of columns.

4. **Adaptive UI**: The table view is reconstructed for each query, ensuring that it always matches the structure of the current result set.

5. **Generic Filtering**: The filtering mechanism works across all columns and adapts to whatever data is currently displayed.

This modular design allows the application to handle a wide variety of SQL queries without needing to be modified for each specific query type or result structure.

## Setup and Usage

1. Ensure you have Java and JavaFX set up in your development environment.
2. Configure your database connection details in the `initDatabaseConnection()` method.
3. Run the application.
4. Enter your SQL query in the text field.
5. Click "Execute Query" to see the results.
6. Use the filter field to narrow down the displayed results in real-time.

## Extending the Application

To extend this application, consider:

- Adding support for database schema exploration
- Implementing query history functionality
- Adding export options for query results
- Implementing more advanced filtering and sorting capabilities

Remember to handle exceptions appropriately and provide user feedback for better usability.
