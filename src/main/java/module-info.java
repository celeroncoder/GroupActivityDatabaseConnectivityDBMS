module com.celeroncoder.groupactivitydatabaseconnectivitydbms {
	requires javafx.controls;
	requires javafx.fxml;
	requires java.sql;


	opens com.celeroncoder.groupactivitydatabaseconnectivitydbms to javafx.fxml;
	exports com.celeroncoder.groupactivitydatabaseconnectivitydbms;
}