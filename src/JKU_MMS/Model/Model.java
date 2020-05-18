package JKU_MMS.Model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Model {
    public Profile currentSettings = new Profile("CURRENT_SETTINGS");
    public ObservableList<Task> tasks = FXCollections.observableArrayList();
}
