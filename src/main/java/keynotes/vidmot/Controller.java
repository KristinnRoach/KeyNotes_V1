package keynotes.vidmot;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import keynotes.vinnsla.Playback;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import keynotes.vinnsla.PlayerTimeline;
import keynotes.vinnsla.Status;
import keynotes.vinnsla.ToolKeys;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

/**
 The KeysUI class is the controller class for the graphical user interface for the keyboard keys and its functionalities.
 It implements the Initializable interface.
 */
public class Controller implements Initializable {

    @FXML
    private BorderPane fxBorderPane;

    @FXML
    private Button fxTransUp, fxTransDown;

    public ToggleButton getFxLoopLock() {
        return fxLoopLock;
    }

    public ToggleButton getFxSustain() {
        return fxSustain;
    }

    public ToggleButton getFxDelay() {
        return fxDelay;
    }

    @FXML
    private ToggleButton fxShowNotes, fxMinorMajor, fxLoopLock, fxSustain, fxDelay;

    @FXML
    private Label fxRootNote, fxTempo;
    @FXML
    private Button fxZ, fxX, fxC, fxV, fxB, fxN, fxM, fxComma, fxDot, fxÞ, fxA, fxS, fxD, fxF, fxG, fxH, fxJ, fxK, fxL, fxÆ, fxQ, fxW, fxE, fxR, fxT, fxY, fxU, fxI, fxO, fxP, fx1, fx2, fx3, fx4, fx5, fx6, fx7, fx8, fx9, fx0;
    public static Button[] getButtons() {
        return buttons;
    }
    private static Button[] buttons;
    private final HashMap<KeyCode, Button> keycode_button_map = new HashMap<>();
    private final int[] keyIndicesMajor = { 0, 2, 4, 5, 7, 9, 11, 12, 14, 16, 12, 14, 16, 17, 19, 21, 23, 24, 26, 28, 24, 26, 28, 29, 31, 33, 35, 36, 38, 40, 36, 38, 40,41, 43, 45, 47, 48, 50, 52 };
    private final Set<Integer> minor = new HashSet<>(Arrays.asList(4, 9, 11, 16, 21, 23, 28, 33, 35, 40, 45, 47, 52));
    private final HashMap<KeyCode, Integer> allNotesMap = new HashMap<>();
    private final HashMap<Button, Integer> buttonIntMap = new HashMap<>();  // einfaldast að nota bara þetta
    private final HashMap <KeyCode, Integer> currentlyPressedMap = new HashMap<>();
    private String[] keyboardKeysIDs; // button ids
    @FXML
    private Button fxQuit, fxMinimize;
    @FXML
    private HBox topBar;
    @FXML
    private Slider fxVolSlider, fxLoopLengthSlider, fxDecaySlider;

    private final KeyCode[] noteKeyCodes = { KeyCode.Z, KeyCode.X, KeyCode.C, KeyCode.V, KeyCode.B, KeyCode.N, KeyCode.M, KeyCode.COMMA, KeyCode.PERIOD, KeyCode.SLASH, KeyCode.A, KeyCode.S, KeyCode.D, KeyCode.F, KeyCode.G, KeyCode.H, KeyCode.J, KeyCode.K, KeyCode.L, KeyCode.SEMICOLON, KeyCode.Q, KeyCode.W, KeyCode.E, KeyCode.R, KeyCode.T, KeyCode.Y, KeyCode.U, KeyCode.I, KeyCode.O, KeyCode.P, KeyCode.DIGIT1, KeyCode.DIGIT2, KeyCode.DIGIT3, KeyCode.DIGIT4, KeyCode.DIGIT5, KeyCode.DIGIT6, KeyCode.DIGIT7, KeyCode.DIGIT8, KeyCode.DIGIT9, KeyCode.DIGIT0 };
    private final Set<KeyCode> toolKeySet = Set.of(KeyCode.TAB, KeyCode.SHIFT, KeyCode.CAPS, KeyCode.SPACE, KeyCode.BACK_QUOTE);
    private boolean isArrowKey(KeyCode keyCode) {
        return keyCode == KeyCode.UP || keyCode == KeyCode.DOWN || keyCode == KeyCode.LEFT || keyCode == KeyCode.RIGHT;
    }

    public static BooleanProperty isMajorProperty() {
        return isMajor;
    }
    private static final IntegerProperty transposition = new SimpleIntegerProperty(0);
    public static int getTransposition() {
        return transposition.get();
    }
    private static final BooleanProperty isMajor = new SimpleBooleanProperty(true);


    /**
     Initializes the GUI components and calls other necessary set-up methods.
     @param url URL location of the FXML file used to build the interface
     @param resourceBundle ResourceBundle used to localize the GUI components
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        fxBorderPane.addEventFilter(KeyEvent.KEY_PRESSED, this::onKeyPressed);
        fxBorderPane.addEventFilter(KeyEvent.KEY_RELEASED, this::onKeyReleased);

        System.out.println("Java Version: "+System.getProperty("java.version"));
        System.out.println("Java Runtime Version: "+System.getProperty("java.runtime.version"));
        System.out.println("JavaFX Version: "+System.getProperty("javafx.runtime.version"));

        Status.setControllerInstance(this);
        initializeButtons();
        initializeMaps();
        setStyleClasses();
        addNoteNameListener();

        setUpVolumeSlider(fxVolSlider);
        setUpLengthSlider();

        TxtMethods.initialize();

        try {
            Playback.initialize();
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
        setUpFocus();
    }

    private void addNoteNameListener() { // má vera í TxtMethods?

        transposition.addListener((observableValue, oldValue, newValue) -> {
            TxtMethods.setTransposition(getTransposition());
            TxtMethods.setNoteNames();
            String minorMajor = isMajor.get() ? "Major" : "minor";
            fxRootNote.setText(TxtMethods.getRootNote() + " " + minorMajor);
        });
        isMajor.addListener((observableValue, oldValue, newValue) -> {
            TxtMethods.setNoteNames();
            String minorMajor = isMajor.get() ? "Major" : "minor";
            fxRootNote.setText(TxtMethods.getRootNote() + " " + minorMajor);
        });
    }


    /**
     * Sets up the focus traversable for certain GUI components.
     */
    private void setUpFocus() {
        fxShowNotes.setFocusTraversable(false);
        fxMinorMajor.setFocusTraversable(false);
        fxTransUp.setFocusTraversable(false);
        fxTransDown.setFocusTraversable(false);
        fxTransDown.setFocusTraversable(false);
        fxLoopLock.setFocusTraversable(false);
        fxQuit.setFocusTraversable(false);
        fxMinimize.setFocusTraversable(false);
        fxVolSlider.setFocusTraversable(false);
        fxLoopLengthSlider.setFocusTraversable(false);
        fxSustain.setFocusTraversable(false);
        fxDelay.setFocusTraversable(false);
    }

    /**
     Initializes the Button[] buttons array and HashMap<KeyCode, Button> buttonMap used to map the KeyCodes to the buttons.
     */
    private void initializeButtons(){
        buttons = new Button[]{fxZ, fxX, fxC, fxV, fxB, fxN, fxM, fxComma, fxDot, fxÞ, fxA, fxS, fxD, fxF, fxG, fxH, fxJ, fxK, fxL, fxÆ, fxQ, fxW, fxE, fxR, fxT, fxY, fxU, fxI, fxO, fxP, fx1, fx2, fx3, fx4, fx5, fx6, fx7, fx8, fx9, fx0};
        int cnt = 0;
        keyboardKeysIDs = new String[buttons.length];
        for(Button b : buttons){
            keyboardKeysIDs[cnt] = b.getText();
            b.setId(keyboardKeysIDs[cnt]);
            buttonIntMap.put(b, (keyIndicesMajor[cnt] + 12));
            cnt++;
        }
    }

    private void initializeMaps() {
        for(int i = 0; i < buttons.length; i++) {
            keycode_button_map.put(noteKeyCodes[i], buttons[i]);
            allNotesMap.put(noteKeyCodes[i], keyIndicesMajor[i]);
        }
    }


    private void setStyleClasses() {

       // fxBorderPane.getStyleClass().add("appBackgroundColor");

        fxShowNotes.getStyleClass().add("showNotes");

        fxDelay.getStyleClass().add("toolbutton");
        fxSustain.getStyleClass().add("toolbutton");
        fxTransUp.getStyleClass().add("toolbutton");
        fxTransDown.getStyleClass().add("toolbutton");
        fxMinorMajor.getStyleClass().add("toolbutton");
        fxLoopLock.getStyleClass().add("toolbutton");

        for (int i = 0; i < 10; i++) {
            buttons[i].getStyleClass().add("oct1");
        }
        for (int i = 10; i < 20; i++) {
            buttons[i].getStyleClass().add("oct2");
        }
        for (int i = 20; i < 30; i++) {
            buttons[i].getStyleClass().add("oct3");
        }
        for (int i = 30; i < 40; i++) {
            buttons[i].getStyleClass().add("oct4");
        }
    }
    /**
     * Called when a key is pressed in the GUI. Determines the corresponding index of the media to be played
     * and plays the corresponding note. Updates the GUI to indicate that the key has been pressed.
     * The keyboard rolloff on many common keyboards is limited to six keys pressed at a time so the
     * pressedKeys size is limited to six.
     *
     * @param keyEvent KeyEvent corresponding to the key that was pressed
     */
    @FXML
    protected void onKeyPressed(KeyEvent keyEvent) {
        KeyCode keyCode = keyEvent.getCode();

        if (keycode_button_map.containsKey(keyCode) && !currentlyPressedMap.containsKey(keyCode)) {
            handleNoteKeyPressed(keyCode);
        } else if (toolKeySet.contains(keyCode)) {
            ToolKeys.handleToolKeyPressed(keyCode, keyEvent);
            if (keyCode == KeyCode.CAPS) {    }
        } else if (isArrowKey(keyCode)) {
            handleArrowKeys(keyCode, true);
        }
        keyEvent.consume();
    }

    private void handleNoteKeyPressed(KeyCode keyCode) {
        int keyIndex = (allNotesMap.get(keyCode) + transposition.get() + 12) % 76;

        if (!isMajor.get() && (minor.contains(keyIndex - transposition.get()))) {
            keyIndex -= 1;
        }

        Playback.playMedia(keyIndex, currentlyPressedMap.size());

        Button button = keycode_button_map.get(keyCode);
        button.getStyleClass().add("buttonPressed");

        currentlyPressedMap.put(keyCode, keyIndex);
    }


    private void handleArrowKeys(KeyCode keyCode, boolean isPressed) {
        ButtonBase button;
        switch (keyCode) {
            case DOWN -> button = fxTransDown;
            case UP -> button = fxTransUp;
            case LEFT -> button = fxTransDown;
            case RIGHT -> button = fxTransUp;
            default -> throw new IllegalArgumentException("Invalid arrow key");
        }
        if (isPressed) {
            button.fire();
            button.getStyleClass().add("toolButtonPressed");
        } else {
            button.getStyleClass().remove("toolButtonPressed");
        }
    }



    @FXML
    protected void onKeyReleased(KeyEvent event) {
        KeyCode keyCode = event.getCode();

        if (currentlyPressedMap.containsKey(keyCode)) {
            handleNoteKeyReleased(keyCode);
        }
        else if (toolKeySet.contains(keyCode)) {
            ToolKeys.handleToolKeyReleased(keyCode, event);
        } else if (isArrowKey(keyCode)) {
            handleArrowKeys(keyCode, false);
        }
        event.consume();
    }
    private void handleNoteKeyReleased(KeyCode keyCode) {
        Button button = keycode_button_map.get(keyCode);
        button.getStyleClass().remove("buttonPressed");

        Playback.noteKeyReleased(currentlyPressedMap.get(keyCode));
        currentlyPressedMap.remove(keyCode);
    }

    @FXML
    private void mousePressedSample(MouseEvent e) {
        Button button = (Button) e.getSource();

        int keyIndex = -1;
        if (button != null) {
            keyIndex = buttonIntMap.get(button) + transposition.get();
        }
        if (keyIndex != -1 && !isMajor.get() && minor.contains(keyIndex - transposition.get())) {
            keyIndex -= 1;
        }
        Playback.playMedia(keyIndex, currentlyPressedMap.size());
    }

    @FXML
    private void fxLoopLockHandler(ActionEvent event) {
        // playback.setLoopLocked(fxLoopLock.isSelected()); // virkar ekki, finna þægilegt system til að vinna með mouseclick vs keypress og listeners
    }

    @FXML
    private void transposeMouseClick(ActionEvent e) {
    if (e.getSource().equals(fxTransUp)) {
        transposition.set((transposition.get() + 1) % (12));
    } else if (e.getSource().equals(fxTransDown)) {
        transposition.set((transposition.get() - 1) % (-12));
    }
    }

    @FXML
    private void resetTransposition(MouseEvent mouseEvent) {
        if(mouseEvent.isAltDown() || mouseEvent.getClickCount() == 2) {
            transposition.set(0);
        }
    }


    @FXML
    private void minorMajorButton(ActionEvent e) {
        if (isMajor.get()) {
            fxMinorMajor.setText("◉︵◉");
            isMajor.set(false);
            fxMinorMajor.getStyleClass().add("toolButtonSelected");
        } else {
            fxMinorMajor.setText("ʘ‿ʘ");
            isMajor.set(true);
            fxMinorMajor.getStyleClass().remove("toolButtonSelected");
        }
    }
    @FXML
    private void noteNamesHandler(ActionEvent e) { // óþarfi
        if (fxShowNotes.getStyleClass().contains("showNotes")) {
            fxShowNotes.getStyleClass().remove("showNotes");
            fxShowNotes.getStyleClass().add("showKeyboard");
            for (int i = 0; i < buttons.length; i++) {
                buttons[i].setText(keyboardKeysIDs[i]);
            }
            TxtMethods.setShowNotes(false);
        } else {
            fxShowNotes.getStyleClass().remove("showKeyboard");
            fxShowNotes.getStyleClass().add("showNotes");
            TxtMethods.setShowNotes(true);
            TxtMethods.setNoteNames();
        }
    }

    public void onFxSustainMouseClick(MouseEvent mouseEvent) {
        Status.setIsSustainOn(fxSustain.isSelected());
    }




    /**
    Sets up a Volume slider to control the main volume of all media-players
     */
    private void setUpVolumeSlider(Slider fxVolSlide) {
        fxVolSlide = new Slider();
        fxVolSlide.setValue(80.0);
        Playback.setMasterVolume(0.8);

        fxVolSlide.valueProperty().addListener((observable, oldValue, newValue) -> {
            double volume = newValue.doubleValue() / 100.0;
            Playback.setMasterVolume(volume);
        });
    }


    public void setUpLengthSlider() {
        PlayerTimeline.setCurrentSliderValue(4);
        fxLoopLengthSlider.showTickLabelsProperty().setValue(true);
        fxLoopLengthSlider.snapToTicksProperty().setValue(true); // why doesnt it snap?

        // Listener for note length changes
        fxLoopLengthSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            PlayerTimeline.setCurrentSliderValue(newValue.intValue());
            PlayerTimeline.setFadeOutLength();
        });
    }


    /**
     * Allows the window to be dragged by clicking and holding the top bar
     * @param event event from the top bar
     */
    @FXML
    private void dragWindow(MouseEvent event) {
        Stage stage = (Stage) topBar.getScene().getWindow();
        double offsetX = event.getSceneX();
        double offsetY = event.getSceneY();

        topBar.setOnMouseDragged(dragEvent -> {
            stage.setX(dragEvent.getScreenX() - offsetX);
            stage.setY(dragEvent.getScreenY() - offsetY);
        });
    }
    /**
     * Quits the application when x in the top left corner is clicked
     */
    @FXML
    private void quitApp() {
        Platform.exit();
    }
    /**
     * Minimizes the application when - in the top left corner is clicked
     */
    @FXML
    private void minimizeClick() {
        Stage stage = (Stage) fxMinimize.getScene().getWindow();
        stage.setIconified(true);
    }

    public void fxDelayHandler(ActionEvent e) {
        boolean isItTrue = fxDelay.isSelected();
        PlayerTimeline.setIsDelayOn(isItTrue);
    }

    public void fxTempoMouseClicked(MouseEvent mouseEvent) {
        if (mouseEvent.getButton() == MouseButton.PRIMARY) {
            fxTempo.setText(DEFAULT_TEMPO + " bpm");
            PlayerTimeline.setTempo(DEFAULT_TEMPO);
            PlayerTimeline.setFadeOutLength();
        }
    }

    public static final int MIN_TEMPO = 40;  // Minimum tempo (BPM)
    public static final int MAX_TEMPO = 240; // Maximum tempo (BPM)
    public static final int DEFAULT_TEMPO = 120; // Default tempo (BPM)

    public void fxTempoMouseDragged(MouseEvent mouseEvent) {
        if (mouseEvent.getButton() == MouseButton.PRIMARY) {
            double deltaY = mouseEvent.getY();
            double tempoChange = -deltaY * 0.5;
            double newTempo = Math.max(MIN_TEMPO, Math.min(DEFAULT_TEMPO + tempoChange, MAX_TEMPO));

            fxTempo.setText((int) newTempo + " bpm");
            PlayerTimeline.setTempo((int) newTempo);
            PlayerTimeline.setFadeOutLength();
        }
    }
}
