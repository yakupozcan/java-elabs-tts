package elevenlabsApp.service;

import javax.sound.sampled.*;
import java.io.IOException;
import java.io.InputStream;

public class AudioPlayerService {

    public void play(InputStream inputStream) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        // Define the audio format for pcm_24000
        // Sample Rate: 24000 Hz, Sample Size: 16 bits, Channels: 1 (Mono), Signed: true, Big Endian: false (Little Endian)
        AudioFormat format = new AudioFormat(24000, 16, 1, true, false);

        // The total length of the audio data in bytes.
        long length = inputStream.available();
        // The length in sample frames.
        long frameLength = length / format.getFrameSize();


        try (AudioInputStream audioStream = new AudioInputStream(inputStream, format, frameLength)) {
            Clip clip = AudioSystem.getClip();

            // Add a listener to close the clip when it finishes playing
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    event.getLine().close();
                }
            });

            clip.open(audioStream);
            clip.start();

            // Wait for the clip to finish playing. This is a blocking call.
            // In a real GUI app, you'd handle this asynchronously.
            while (clip.isRunning()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
