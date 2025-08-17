package elevenlabsApp.service;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class AudioFileManager {

    public void saveAudioStream(InputStream audioStream, Path filePath) throws IOException {
        byte[] audioBytes = audioStream.readAllBytes();
        AudioFormat format = new AudioFormat(24000, 16, 1, true, false);
        try (AudioInputStream audioInputStream = new AudioInputStream(new ByteArrayInputStream(audioBytes), format, audioBytes.length / format.getFrameSize())) {
            AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, filePath.toFile());
        } catch (IOException e) {
            throw new IOException("Failed to save audio file.", e);
        }
    }

    public void play(Path filePath) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        File audioFile = filePath.toFile();
        try (AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile)) {
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
            // Add a listener to close the clip when it's done
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    clip.close();
                }
            });
        }
    }
}
