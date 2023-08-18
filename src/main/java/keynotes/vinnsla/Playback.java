package keynotes.vinnsla;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import keynotes.vidmot.Controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

import static keynotes.vinnsla.Status.*;

public class Playback {

    private static String samplePackPath;
    public static void setSamplePackPath(String path) {
        samplePackPath = path;
    }
    private static final int nrOfSamples = 76;
    private static final Media[] samples = new Media[nrOfSamples];
    private static final SamplePlayer[] samplePack = new SamplePlayer[nrOfSamples]; // prófa
    public static Media[] getSamples() {
        return samples;
    }
    private static final int MAX = 40; // number of simultaneously playing MediaPlayers allowed
    private static final int keyboardRollOver = 6; // number of keys that can be pressed at the same time on a macbook keyboard

    private static final Set<SamplePlayer> playingPlayersSet = new HashSet<>(); // set of currently playing SamplePlayers
    private static final Map<Integer, SamplePlayer> currentlyPlayingMap = new HashMap<>(); // set of currently playing SamplePlayers
    private static final Queue<SamplePlayer> playingFIFO = new LinkedList<>();
    public static void addToCurrentlyPlaying(SamplePlayer samplePlayer) {
        playingPlayersSet.add(samplePlayer);
        currentlyPlayingMap.put(samplePlayer.mediaIndex, samplePlayer);
        // System.out.println("currentlyPlayingMap added: " + samplePlayer + " with index: " + samplePlayer.mediaIndex);
        playingFIFO.add(samplePlayer);
    }
    public static void removeFromCurrentlyPlaying(SamplePlayer samplePlayer) {
        playingPlayersSet.remove(samplePlayer);
        currentlyPlayingMap.remove(samplePlayer.mediaIndex);
        // System.out.println("currentlyPlayingMap removed: " + samplePlayer + " with index: " + samplePlayer.mediaIndex);
        playingFIFO.remove(samplePlayer);
    }

    private static double masterVolume; // volume of all MediaPlayers
    public static double getMasterVolume() {
        return masterVolume;
    }

    private static MediaPlayer metronome;

    private Playback() { throw new IllegalStateException("Utility class"); }

    // NEW VERSION OF importSamplePack() THAT SHOULD WORK WITH JARs

    public static void importSamplePack() throws URISyntaxException, IOException {
        URL url = ClassLoader.getSystemResource(samplePackPath);
        if (url == null) { throw new FileNotFoundException("SamplePack not found in samplepack path: " + samplePackPath); }

        URI uri = url.toURI();

        if (uri.getScheme().equals("jar")) {
            try (FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.<String, Object>emptyMap())) {
                Path path = fileSystem.getPath(samplePackPath);
                importFilesFromPath(path);
            }
        } else {
            Path path = Paths.get(uri);
            importFilesFromPath(path);
        }
    }

    private static void importFilesFromPath(Path path) throws IOException {
        try (Stream<Path> paths = Files.walk(path)) {
            List<Path> files = paths
                    .filter(filePath -> filePath.toString().endsWith(".wav") || filePath.toString().endsWith(".mp3"))
                    .sorted()
                    .toList();

            for (int i = 0; i < files.size(); i++) {
                Media media = new Media(files.get(i).toUri().toString());
                samples[i] = media;
                samplePack[i] = new SamplePlayer(samples[i], i, false);
            }
        }
    }


    public static synchronized void setMasterVolume(double volume) {

        SamplePlayer.setMasterVolume(volume);

        masterVolume = volume;

        for (SamplePlayer player : playingPlayersSet) {
          //  if (player.getVolume() == masterVolume) { // && masterVolume != 0 // er þetta besta leiðin?
                player.setVolume(masterVolume);
          //  }
        }
      //  masterVolume = volume; // ætti þetta að vera fyrir ofan for loopuna?
    }

    // public static SamplePlayer mostRecentPlayer;
    // public static SamplePlayer previousPlayer;

    public static synchronized void playMedia(int mediaIndex, int nrOfKeysPressed) {

        if (nrOfKeysPressed <= keyboardRollOver && currentlyPlayingMap.size() < MAX) {
            SamplePlayer player;

            if ( !samplePack[mediaIndex].isPlaying) { // use an existing mediaplayer if it is not already playing
                player = samplePack[mediaIndex];
                player.setVolume(masterVolume);
            }
            else {
                player = new SamplePlayer(samples[mediaIndex], mediaIndex, true);
             }
            player.play();
            player.isPlaying = true;
            player.setStartTime();
            // previousPlayer = mostRecentPlayer;
            // mostRecentPlayer = player;

            if(!isSustainOn) {
                player.playerTimeline.startFadeOut();
            } else if (isLoopOn && !isFreePlayOn) {
                releaseCurrentlyLooping();
            }

            addToCurrentlyPlaying(player);

            //System.out.println(currentlyPlayingMap.size());
        }
    }

    public static synchronized void noteKeyReleased(int mediaIndex) {
        if (isSustainOn) {
            if (currentlyPlayingMap.containsKey(mediaIndex)) { //  && currentlyPlayingMap.get(mediaIndex).isPlayingOrLooping  má líklega henda
                currentlyPlayingMap.get(mediaIndex).playerTimeline.startFadeOut();
            } else {
                System.out.println("ERROR: Could not find playing player to be released on noteKey release. Does playing map contain player? " + currentlyPlayingMap.containsKey(mediaIndex));
            }
        }
    } // eitthvað kúl með delay?

    public static void releaseCurrentlyLooping() {
        for (SamplePlayer player : playingFIFO) {
            if (player.getElapsedTime() > PlayerTimeline.getFadeOutLength() ) {
                // System.out.println("player.getElapsedTime() == " + player.getElapsedTime());
                player.isReleased = true;
            }
        }
    }

    public static void initialize() throws URISyntaxException, IOException {
        setSamplePackPath("keynotes/vidmot/Audio/pno_mp3_C1-C7");
        importSamplePack();
        PlayerTimeline.setTempo(Controller.DEFAULT_TEMPO);
        PlayerTimeline.setFadeOutLength();
    }

}
