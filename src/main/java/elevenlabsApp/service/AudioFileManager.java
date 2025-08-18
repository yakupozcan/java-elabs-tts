package elevenlabsApp.service;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class AudioFileManager {

    public void saveAudioStream(InputStream audioStream, Path filePath) throws IOException {
        byte[] audioBytes = readAllBytes(audioStream);
        AudioFormat format = new AudioFormat(24000, 16, 1, true, false);
        try (AudioInputStream audioInputStream = new AudioInputStream(new ByteArrayInputStream(audioBytes), format, audioBytes.length / format.getFrameSize())) {
            AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, filePath.toFile());
        } catch (IOException e) {
            throw new IOException("Failed to save audio file.", e);
        }
    }

    private byte[] readAllBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384];
        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        return buffer.toByteArray();
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
