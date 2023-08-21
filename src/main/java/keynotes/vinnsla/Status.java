package keynotes.vinnsla;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.ToggleButton;
import keynotes.vidmot.Controller;

public class Status {
    private static Controller controllerInstance;

    public static void setControllerInstance(Controller controller) {
        controllerInstance = controller;
    }

    static boolean isLoopOn;
    static boolean isLoopLocked;
    static boolean isSustainOn;
    static boolean isDelayOn;
    static boolean isFreePlayOn = false;



    public static void setIsSustainOn(boolean b) {
        isSustainOn = b;
        System.out.println("sustain: " + b);
    }

    // METHODS

    static void handleCaps(boolean b) {
        isLoopLocked = b;
        isLoopOn = b;
        ToggleButton fxLoopLock = controllerInstance.getFxLoopLock();
        fxLoopLock.setSelected(b);
        if (!b) {
            Playback.releaseCurrentlyLooping();
            isFreePlayOn = false;
        }
        // System.out.println("CapsOn : Loop Locked" + b);
    }

     static void handleSpace(boolean isSpaceOn, boolean isShiftDown) {
         //ToggleButton fxLoopLock = controllerInstance.getFxLoopLock(); // setSelected

         if (!isLoopLocked) {
             isLoopOn = isSpaceOn;
         } else if (isSpaceOn) {
             if (isShiftDown || isSustainOn && !isFreePlayOn) {
                 isFreePlayOn = true;
             } else {
                 Playback.releaseCurrentlyLooping(); // spurning hvort er þægilegra að sleppa eða hafa hér
                 isFreePlayOn = false;
             }
             System.out.println("Status.isFreePlayOn? : " + isFreePlayOn);
         }
     }

    static void handleShift(boolean b) {
        if (b) {
           // Controller.getFxSyncNSpace().getStyleClass().add("toolButtonSelected");
        } else {
           // Controller.getFxSyncNSpace().getStyleClass().remove("toolButtonSelected");
        }

    }
    static void handleTab(boolean b) {
        setIsSustainOn(b);
        ToggleButton fxSustain = controllerInstance.getFxSustain();
        fxSustain.setSelected(b);
    }
    static void handleBackQuote(boolean b) {
        isDelayOn = b;
        PlayerTimeline.setIsDelayOn(b);
        ToggleButton fxDelay = controllerInstance.getFxDelay();
        fxDelay.setSelected(b);
    }
}

/*
    static void listenForLoopReleases(AtomicBoolean loopReleased) {
        isLoopOnProperty.addListener((observable, oldValue, newValue) -> {
            if (oldValue && !newValue) {
                loopReleased.set(true);
            }
        });
        isSustainOnProperty.addListener((observable, oldValue, newValue) -> {
            if (oldValue && !newValue) {
                loopReleased.set(true);
            }
        });
    } */

