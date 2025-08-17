
package elevenlabsApp;

import elevenlabsApp.config.Configuration;
import elevenlabsApp.service.AudioPlayerService;
import elevenlabsApp.service.ElevenLabsService;

import javax.swing.*;
import java.awt.*;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;

public class Main {

    private final Configuration config;
    private final ElevenLabsService elevenLabsService;
    private final AudioPlayerService audioPlayerService;

    private JFrame frame;
    private JTextField textField;
    private JButton anonsButonu;

    public Main() {
        // Initialize services
        this.config = new Configuration();
        this.elevenLabsService = new ElevenLabsService();
        this.audioPlayerService = new AudioPlayerService();
    }

    public static void main(String[] args) {
        try {
            // Check config on startup
            Configuration config = new Configuration();
            config.getApiKey(); // This will throw an exception if the key is not set
            SwingUtilities.invokeLater(() -> new Main().createAndShowGUI());
        } catch (RuntimeException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Hata: " + e.getMessage(), "Konfigürasyon Hatası", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void createAndShowGUI() {
        frame = new JFrame("ElevenLabs Anons Sistemi");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 150);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel label = new JLabel("Anons Edilecek Metni Girin:");
        label.setFont(new Font("Arial", Font.BOLD, 14));

        textField = new JTextField();
        textField.setFont(new Font("Arial", Font.PLAIN, 14));

        anonsButonu = new JButton("Sesi Oluştur");
        anonsButonu.setFont(new Font("Arial", Font.BOLD, 14));

        panel.add(label, BorderLayout.NORTH);
        panel.add(textField, BorderLayout.CENTER);
        panel.add(anonsButonu, BorderLayout.SOUTH);

        frame.getContentPane().add(panel);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        anonsButonu.addActionListener(e -> performTextToSpeech());
    }

    private void performTextToSpeech() {
        String text = textField.getText();
        if (text.trim().isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Lütfen anons edilecek bir metin girin.", "Uyarı", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Disable button and change text
        anonsButonu.setEnabled(false);
        anonsButonu.setText("Oluşturuluyor...");

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                String apiKey = config.getApiKey();
                InputStream audioStream = elevenLabsService.textToSpeech(apiKey, text);
                audioPlayerService.play(audioStream);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get(); // This will re-throw any exception from doInBackground
                } catch (ExecutionException e) {
                    // Exception from our services
                    e.printStackTrace();
                    Throwable cause = e.getCause();
                    JOptionPane.showMessageDialog(frame, "Hata oluştu: " + cause.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
                } catch (InterruptedException e) {
                    // Thread was interrupted
                    Thread.currentThread().interrupt(); // Preserve the interrupted status
                } finally {
                    // Re-enable button and restore text
                    anonsButonu.setEnabled(true);
                    anonsButonu.setText("Sesi Oluştur");
                }
            }
        };

        worker.execute();
    }
}