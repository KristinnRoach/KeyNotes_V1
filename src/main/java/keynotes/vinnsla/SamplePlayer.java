package keynotes.vinnsla;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

class SamplePlayer {  // helper class for samples


    // FIELDS

    public Media media;
    public int mediaIndex;

    public MediaPlayer mp;
    public double startTime;
    public boolean isCopy;
    public boolean isPlaying;
    public boolean isReleased;

    public SamplePlayer previousPlayer;

    public PlayerTimeline playerTimeline;

    public static void setMasterVolume(double masterVolume) {
        SamplePlayer.masterVolume = masterVolume;
    }

    private static double masterVolume;

    // CONSTRUCTORS


    public SamplePlayer(Media media, int mediaIndex, boolean isCopy) {
        this.media = media;
        this.mp = new MediaPlayer(media);
        this.mediaIndex = mediaIndex;
        this.setVolume(SamplePlayer.masterVolume);
        this.startTime = -1;
        this.isCopy = isCopy;
        this.isReleased = false;
        this.playerTimeline = new PlayerTimeline(this); // creates timeline for this specific mediaplayers volume fade out
        setupMediaPlayer();
        //addListeners();
    }


    // CUSTOM METHODS

    /*
        private void addListeners() {
            Status.getIsLoopOn().addListener((observable, oldValue, newValue) -> {
                isLooping = newValue;
                System.out.println("isLooping = " + newValue);
            });


            Status.getLoopReleased().addListener((observable, oldValue, newValue) -> {
                //isLooping = !Status.getLoopReleased().get();
                if (playerTimeline != null) {
                    this.playerTimeline.releaseLoop();
                    System.out.println("release loop ");
                }
            });
        }
    */
    public double getElapsedTime() {
        return System.currentTimeMillis() - startTime;
    }

    public synchronized void setupMediaPlayer() {
        mp.setOnEndOfMedia(mp::stop);
        mp.setOnStopped(() -> {
            mp.seek(Duration.ZERO);
            if (!Status.isLoopOn) {
                this.startTime = -1;
                this.isPlaying = false;
                Playback.removeFromCurrentlyPlaying(this);
                if (this.isCopy) {
                    mp.dispose();
                }
            }
        });
    }
    // METHODS

    synchronized void replay() {
        //mp.stop(); // seek gerist í onstopped
        mp.seek(Duration.ZERO);
        mp.setVolume(masterVolume);
        mp.play();
    }

    // CUSTOM GETTERS AND SETTERS


    public void setStartTime() {
        this.startTime = System.currentTimeMillis();
    }


    // MEDIAPLAYER METHODS


    public synchronized void play() {
        mp.play();
        //isMostRecentlyPlayed = true;
    }

    public synchronized void stop() {
        mp.stop();
    }

    public void dispose() {
        mp.dispose();
    }

    public Media getMedia() {
        return mp.getMedia();
    }

    public synchronized void setVolume(double volume) {
        mp.setVolume(volume);
    }

    public synchronized double getVolume() {
        return mp.getVolume();
    }

    public void setRate(double rate) {
        mp.setRate(rate);
    }

    public synchronized void seek(Duration duration) {
        mp.seek(duration);
    }

    public void setOnPlaying(Runnable r) {
        mp.setOnPlaying(r);
    }

    public void setOnEndOfMedia(Runnable r) {
        mp.setOnEndOfMedia(r);
    }
}