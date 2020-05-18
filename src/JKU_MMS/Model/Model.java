package JKU_MMS.Model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Holds all data while the program is running
 */
public class Model {
    public Profile currentSettings = new Profile("CURRENT_SETTINGS");
    public ObservableList<Task> tasks = FXCollections.observableArrayList();
}
