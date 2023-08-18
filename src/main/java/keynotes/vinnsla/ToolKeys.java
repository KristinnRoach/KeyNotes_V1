package keynotes.vinnsla;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.HashSet;
import java.util.Set;


public class ToolKeys {

    // CONSTRUCTOR


    // FIELDS

    private static boolean isSpaceOn = false;
    private static boolean isTabOn = false;
    private static boolean isBackQuoteOn = false;
    private static boolean isCapsOn = false;
    private static boolean isShiftOn = false;

    private static final Set<KeyCode> currentlyPressed = new HashSet<>();


    // KEY METHODS


    private static void toggleTab() {
        isTabOn = !isTabOn;
    }

    private static void toggleBackQuote() {
        isBackQuoteOn = !isBackQuoteOn;
    }

    private static void toggleCaps() {
        isCapsOn = !isCapsOn;
    }

    private static void toggleShift() {
        isShiftOn = !isShiftOn;
    }


    public static void handleToolKeyPressed(KeyCode keyCode, KeyEvent event) {
        if (!currentlyPressed.contains(keyCode)) {
            if (keyCode == KeyCode.SHIFT) {
                Status.handleShift(event.isShiftDown()); // e.isShiftDown is more reliable if its being held down, if its a toggle key then use true/false ok
            } else if (keyCode == KeyCode.SPACE) {
                isSpaceOn = true;
                Status.handleSpace(isSpaceOn, event.isShiftDown());
            } else if (keyCode == KeyCode.CAPS) {
                //Playback.handleCaps(Toolkit.getDefaultToolkit().getLockingKeyState(java.awt.event.KeyEvent.VK_CAPS_LOCK));
                toggleCaps();
                Status.handleCaps(isCapsOn);
            } else if (keyCode == KeyCode.TAB) {
                toggleTab();
                Status.handleTab(isTabOn);
            } else if (keyCode == KeyCode.BACK_QUOTE) {
                toggleBackQuote();
                Status.handleBackQuote(isBackQuoteOn);
            }
            currentlyPressed.add(keyCode);
        }
    }

    public static void handleToolKeyReleased(KeyCode keyCode, KeyEvent event) {
        if (currentlyPressed.contains(keyCode)) {

            if (keyCode == KeyCode.SPACE) {
                isSpaceOn = false;
                Status.handleSpace(false, event.isShiftDown());

            } else if (keyCode == KeyCode.SHIFT) {
                Status.handleShift(event.isShiftDown());
            }
            currentlyPressed.remove(keyCode);
        }
    }

}
