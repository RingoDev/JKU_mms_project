Because JavaFX is not included in the JKD anymore building the .jar file as an JavaFX application is not supported by intellij :/

In order to run the .jar file javafx has to be downloaded and then the .jar file can be executed like:

java --module-path /home/joel/Downloads/openjfx-11.0.2_linux-x64_bin-sdk/javafx-sdk-11.0.2/lib --add-modules javafx.controls,javafx.fxml -jar JKU_mms_project.jar

Where the module-path has to be changed to the location of the javafx download.
