package elevenlabsApp.service;

import javax.sound.sampled.*;
import java.io.IOException;
import java.io.InputStream;

public class AudioPlayerService {

    public void play(InputStream inputStream) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        try (AudioInputStream audioStream = AudioSystem.getAudioInputStream(inputStream)) {
            Clip clip = AudioSystem.getClip();

            // Add a listener to close the clip when it finishes playing
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    event.getLine().close();
                }
            });

            clip.open(audioStream);
            clip.start();
        }
    }
}
