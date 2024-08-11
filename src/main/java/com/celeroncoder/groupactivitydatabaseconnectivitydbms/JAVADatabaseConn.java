package com.celeroncoder.groupactivitydatabaseconnectivitydbms;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

import java.sql.*;

import java.io.IOException;

public class JAVADatabaseConn extends Application {
	private Connection conn;
	private TableView<ObservableList<String>> tableView;
	private TextField filterField;
	private TextField queryField;
	private ObservableList<ObservableList<String>> masterData;

	public static void main(String[] args) {
		launch();
	}

	@Override
	public void start(@SuppressWarnings("exports") Stage stage) throws IOException {
		stage.setTitle("Database Query GUI");

		queryField = new TextField();
		queryField.setPromptText("Enter your SQL query here...");
		Button executeButton = new Button("Execute Query");
		filterField = new TextField();
		filterField.setPromptText("Filter results...");
		tableView = new TableView<ObservableList<String>>();

		VBox vbox = new VBox(10);
		vbox.setPadding(new Insets(10));
		vbox.getChildren().addAll(queryField, executeButton, filterField, tableView);

		// Set up the scene
		Scene scene = new Scene(vbox, 800, 600);
		stage.setScene(scene);

		executeButton.setOnAction(e -> executeQuery());
		filterField.textProperty().addListener((observable, oldValue, newValue) -> filterResults());

		initDatabaseConnection();

		stage.show();
	}

	private void initDatabaseConnection() {
		// Replace these with your actual database credentials
		String url = "jdbc:mysql://localhost:3306/dbms_grp_activity";
		String user = "root";
		String password = "password";

		try {
			conn = DriverManager.getConnection(url, user, password);
		} catch (SQLException e) {
			e.printStackTrace();
			showAlert("Database Connection Error", "Failed to connect to the database.");
		}
	}

	private void executeQuery() {
		String query = queryField.getText();
		if (query.trim().isEmpty()) {
			showAlert("Query Error", "Please enter a valid SQL query.");
			return;
		}

		try (Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(query)) {

			// Clear existing columns and data
			tableView.getColumns().clear();
			masterData = FXCollections.observableArrayList();

			// Get metadata
			ResultSetMetaData metaData = rs.getMetaData();
			int columnCount = metaData.getColumnCount();

			// Create columns dynamically
			for (int i = 1; i <= columnCount; i++) {
				final int j = i - 1;
				TableColumn<ObservableList<String>, String> col = new TableColumn<>(metaData.getColumnName(i));
				col.setCellValueFactory(param -> {
					ObservableList<String> row = param.getValue();
					return new javafx.beans.property.SimpleStringProperty(
							row.size() > j ? row.get(j) : "");
				});
				tableView.getColumns().add(col);
			}

			// Add data to the master data
			while (rs.next()) {
				ObservableList<String> row = FXCollections.observableArrayList();
				for (int i = 1; i <= columnCount; i++) {
					row.add(rs.getString(i));
				}
				masterData.add(row);
			}

			// Set up filtered data
			FilteredList<ObservableList<String>> filteredData = new FilteredList<>(masterData, p -> true);
			tableView.setItems(filteredData);

			// Set up filter listener
			filterField.textProperty().addListener((observable, oldValue, newValue) -> {
				filteredData.setPredicate(row -> {
					if (newValue == null || newValue.isEmpty()) {
						return true;
					}
					String lowerCaseFilter = newValue.toLowerCase();
					return row.stream().anyMatch(cell -> cell.toLowerCase().contains(lowerCaseFilter));
				});
			});

		} catch (SQLException e) {
			e.printStackTrace();
			showAlert("Query Execution Error", "Failed to execute the query: " + e.getMessage());
		}
	}

	private void filterResults() {
		String filterText = filterField.getText().toLowerCase();
		ObservableList<ObservableList<String>> allData = tableView.getItems();
		ObservableList<ObservableList<String>> filteredData = FXCollections.observableArrayList();

		for (ObservableList<String> row : allData) {
			boolean matches = false;
			for (String cell : row) {
				if (cell.toLowerCase().contains(filterText)) {
					matches = true;
					break;
				}
			}
			if (matches) {
				filteredData.add(row);
			}
		}

		tableView.setItems(filteredData);
	}

	private void showAlert(String title, String content) {
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(content);
		alert.showAndWait();
	}
}