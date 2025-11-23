package demo.audio;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import java.io.File;

// Gère la musique de fond du jeu
public class AudioManager {
    
    private MediaPlayer backgroundMusic;
    private boolean musicEnabled = true;
    private double volume = 0.5;
    
    public void playBackgroundMusic(int level) {
        stopCurrentMusic();
        
        String musicFile = getMusicFileForLevel(level);
        if (musicFile != null) {
            try {
                File file = new File(musicFile);
                if (file.exists()) {
                    Media media = new Media(file.toURI().toString());
                    backgroundMusic = new MediaPlayer(media);
                    backgroundMusic.setVolume(volume);
                    backgroundMusic.setCycleCount(MediaPlayer.INDEFINITE);
                    backgroundMusic.play();
                    System.out.println("Musique niveau " + level + " lancée");
                } else {
                    System.out.println("Fichier audio introuvable: " + musicFile);
                }
            } catch (Exception e) {
            }
        });
    }
    
    private String getMusicFileForLevel(int level) {
        return switch (level) {
            case 1 -> "resources/sound/CityOfTears_OST.mp3";
            case 2 -> "resources/sound/RadianceVoidLevel_OST.mp3";
            case 3 -> "resources/sound/NightmareLevel_OST.mp3";
            default -> null;
        };
    }
    
    public void stopCurrentMusic() {
        if (backgroundMusic != null) {
            backgroundMusic.stop();
            backgroundMusic = null;
        }
    }
    
    public void pauseMusic() {
        if (backgroundMusic != null && backgroundMusic.getStatus() == MediaPlayer.Status.PLAYING) {
            backgroundMusic.pause();
        }
    }
    
    public void resumeMusic() {
        if (backgroundMusic != null && backgroundMusic.getStatus() == MediaPlayer.Status.PAUSED) {
            backgroundMusic.play();
        }
    }
    
    public void setVolume(double volume) {
        this.volume = Math.max(0.0, Math.min(1.0, volume));
        if (backgroundMusic != null) {
            backgroundMusic.setVolume(this.volume);
        }
    }
    
    public void setMusicEnabled(boolean enabled) {
        this.musicEnabled = enabled;
        if (!enabled) {
            stopCurrentMusic();
        }
    }
    
    public boolean isMusicEnabled() {
        return musicEnabled;
    }
}