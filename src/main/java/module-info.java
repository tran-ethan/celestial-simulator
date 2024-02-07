module edu.vanier.eastwest {
    requires javafx.controls;
    requires javafx.fxml;
            
                            
    opens edu.vanier.eastwest to javafx.fxml;
    exports edu.vanier.eastwest;
}