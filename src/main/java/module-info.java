module KeyNotes {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires java.desktop;

    opens keynotes.vidmot to javafx.fxml;
    exports keynotes.vidmot;
    exports keynotes.vinnsla;
}