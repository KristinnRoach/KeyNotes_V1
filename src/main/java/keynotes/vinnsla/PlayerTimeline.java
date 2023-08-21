package keynotes.vinnsla;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.util.Duration;

import java.util.HashSet;
import java.util.Set;


public class PlayerTimeline {

    public static Set<PlayerTimeline> playingTimelines = new HashSet<>();

    // Static Fields
    private static int currentSliderValue;

    public static void setCurrentSliderValue(int value) {
        currentSliderValue = value;
    }
    private static int tempo;
    public static void setTempo(int tmpo) {
        tempo = tmpo;
    }
    public static double getFadeOutLength() {
        return fadeOutLength;
    }

    public static void setFadeOutLength() { // slidervalue ranges from 1 - 8

        double quarterNoteDuration = 60000.0 / tempo; // tempo default 120

        fadeOutLength = (quarterNoteDuration / 2) * currentSliderValue;

        // System.out.println("bpm: " + tempo + " length in millisec: " + fadeOutLength + " slider value " + currentSliderValue);
    }

    private static double fadeOutLength;
    private static int delayCycles = 3; // GUI slider
    private static boolean isDelayOn;
    public static void setIsDelayOn(boolean isIt){
        isDelayOn = isIt;
    }
/*
    static boolean isFreePlayStaticOn = Status.isFreePlayOn;
    public static void setFreePlayStatic(boolean isItOn) {
        isFreePlayStaticOn = isItOn;
    }
*/


    // non-static Fields

    public SamplePlayer player;
    public Timeline timeline;
    private boolean loopOnStartOfFade;
    private boolean loopReleased = false;
    private final int initialTempo;
    private int initialSliderValue;

    public void setFreePlayOn(boolean freePlayOn) {
        isFreePlayOn = freePlayOn;
    }

    private boolean isFreePlayOn;


    // Constructor
    public PlayerTimeline(SamplePlayer player) {
        this.player = player;
        this.timeline = new Timeline();
        this.initialTempo = tempo;
        //addFadeKeyFrames();
    }

    // Methods

    synchronized void releaseLoop() {
        this.loopReleased = true;
    }

    synchronized void startFadeOut() {
        turnOnOffDelay(this);

        this.isFreePlayOn = Status.isFreePlayOn;
        this.initialSliderValue = currentSliderValue;
        addFadeKeyFrames(); // could be here depending on witch gives better performance
        loopOnStartOfFade = Status.isLoopOn;

        timeline.play();
        playingTimelines.add(this);
        setOnFinishedFade();
    }

     synchronized void addFadeKeyFrames() {
        double initialVolume = Playback.getMasterVolume();
        double fadeDuration = fadeOutLength;
        //double fadeDuration = ((60000.0 / tempo) / 2 ) * initialSliderValue; // could be fadeOutLength but this follows tempo changes

        int numSteps = 100; // Number of steps for the fade-out
        double scaleFactor = Math.pow(0.0001 / initialVolume, 0.8 / numSteps); // Exponential scale factor

        for (int i = 0; i < numSteps; i++) {
            if (player.getVolume() <= 0.0000001) {
                break;
            }  // ensure that volume does not become negative

            double volume = initialVolume * Math.pow(scaleFactor, i);
            double stepDuration = fadeDuration / numSteps; // duration for each step
            Duration time = Duration.millis(stepDuration * i);
            KeyFrame keyFrame = new KeyFrame(time, event -> {
                player.setVolume(volume);
            });
            timeline.getKeyFrames().add(keyFrame);
        }
    }

    synchronized void tempoSync() {
        if (initialTempo != tempo) {
            timeline.getKeyFrames().clear();
            addFadeKeyFrames();
        }
    }


    synchronized void setOnFinishedFade() { // synchronized?
        timeline.setOnFinished(event -> {
            turnOnOffDelay(this);

            if (isFreePlayOn || !Status.isLoopOn || player.isReleased) {
                stopFadeOut();
            }
            else if (loopOnStartOfFade) {   // check if 'Loop' is STILL on and has not been released since note started
                //tempoSync();
                loopFadeOut();
            }
        });
    }

    synchronized void stopFadeOut() {
        player.stop();
        timeline.stop();
        playingTimelines.remove(this);
    }

    synchronized void loopFadeOut() {
        player.replay();
        timeline.playFromStart();
    }

    static synchronized void turnOnOffDelay(PlayerTimeline pt) {
        if(isDelayOn){
            pt.timeline.setCycleCount(delayCycles);
        } else {
            pt.timeline.setCycleCount(1);
        }
    }

}

