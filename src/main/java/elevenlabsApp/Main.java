package elevenlabsApp;

import elevenlabsApp.config.ApiKeysManager;
import elevenlabsApp.config.SettingsManager;
import elevenlabsApp.service.AudioFileManager;
import elevenlabsApp.service.ElevenLabsService;
import elevenlabsApp.view.SettingsDialog;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class Main {

    // Services and Managers
    private final SettingsManager settingsManager;
    private final ApiKeysManager apiKeysManager;
    private final ElevenLabsService elevenLabsService;
    private final AudioFileManager audioFileManager;

    // UI Components
    private JFrame frame;
    private JTextArea textArea;
    private JButton generateButton;
    private JButton playButton;
    private JButton settingsButton;

    // State
    private Path lastGeneratedAudioPath;

    public Main() {
        this.settingsManager = new SettingsManager();
        this.apiKeysManager = new ApiKeysManager();
        this.elevenLabsService = new ElevenLabsService();
        this.audioFileManager = new AudioFileManager();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().createAndShowGUI());
    }

    private void createAndShowGUI() {
        frame = new JFrame("ElevenLabs Anons Sistemi");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 300);
        frame.setLocationRelativeTo(null);

        // Main Panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Initialize buttons early
        generateButton = new JButton("Sesi Oluştur");
        playButton = new JButton("Oynat");
        settingsButton = new JButton("Ayarlar");
        playButton.setEnabled(false);

        // Header Panel
        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);

        // Text Area
        textArea = new JTextArea();
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(textArea);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Button Panel (Generate and Play)
        JPanel bottomButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        bottomButtonPanel.add(generateButton);
        bottomButtonPanel.add(playButton);
        mainPanel.add(bottomButtonPanel, BorderLayout.SOUTH);

        frame.getContentPane().add(mainPanel);

        // Action Listeners
        generateButton.addActionListener(e -> generateSound());
        playButton.addActionListener(e -> playSound());
        settingsButton.addActionListener(e -> openSettings());

        frame.setVisible(true);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        // 1. Logo
        java.net.URL logoUrl = getClass().getResource("/logo.png");
        JLabel logoLabel;
        if (logoUrl != null) {
            ImageIcon originalIcon = new ImageIcon(logoUrl);
            Image originalImage = originalIcon.getImage();
            Image scaledImage = originalImage.getScaledInstance(-1, 48, Image.SCALE_SMOOTH);
            logoLabel = new JLabel(new ImageIcon(scaledImage));
        } else {
            logoLabel = new JLabel(" "); // Prevents error if logo not found
        }
        headerPanel.add(logoLabel, BorderLayout.WEST);

        // 2. Title
        JLabel titleLabel = new JLabel("<html><div style='text-align: center;'><b>NKÜ HASTANESİ BİLGİ İŞLEM ANONS SİSTEMİ</b></div></html>");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        // 3. Settings Button
        headerPanel.add(settingsButton, BorderLayout.EAST);

        return headerPanel;
    }

    private void openSettings() {
        SettingsDialog dialog = new SettingsDialog(frame, settingsManager, apiKeysManager, elevenLabsService);
        dialog.setVisible(true);
        // The dialog is modal, so code execution will pause here until it's closed.
        // Settings are saved within the dialog itself.
    }

    private void generateSound() {
        // 1. Get all settings from the SettingsManager
        String apiKeyName = settingsManager.getProperty("selectedApiKeyName", null);
        String voiceId = settingsManager.getProperty("selectedVoiceId", null);

        if (apiKeyName == null || apiKeyName.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Lütfen ayarlardan bir API anahtarı seçin.", "Hata", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String apiKey = apiKeysManager.getApiKey(apiKeyName);
         if (apiKey == null) {
            JOptionPane.showMessageDialog(frame, "Seçili API anahtarı bulunamadı.", "Hata", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (voiceId == null || voiceId.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Lütfen ayarlardan bir ses seçin.", "Hata", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String text = textArea.getText();
        if (text.trim().isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Lütfen anons edilecek bir metin girin.", "Uyarı", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Get voice settings
        try {
            double stability = Double.parseDouble(settingsManager.getProperty("stability", "0.75"));
            double similarity = Double.parseDouble(settingsManager.getProperty("similarityBoost", "0.75"));
            double style = Double.parseDouble(settingsManager.getProperty("style", "0.0"));
            boolean useSpeakerBoost = Boolean.parseBoolean(settingsManager.getProperty("useSpeakerBoost", "true"));
            ElevenLabsService.VoiceSettings voiceSettings = new ElevenLabsService.VoiceSettings(stability, similarity, style, useSpeakerBoost);

            // 2. Execute in background
            generateButton.setEnabled(false);
            generateButton.setText("Oluşturuluyor...");
            playButton.setEnabled(false);

            SwingWorker<Path, Void> worker = new SwingWorker<Path, Void>() {
                @Override
                protected Path doInBackground() throws Exception {
                    InputStream audioStream = elevenLabsService.textToSpeech(apiKey, text, voiceId, voiceSettings);
                    File runningDir = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile();
                    Path outputPath = Paths.get(runningDir.getAbsolutePath(), "anons_" + UUID.randomUUID().toString().substring(0, 8) + ".wav");
                    audioFileManager.saveAudioStream(audioStream, outputPath);
                    return outputPath;
                }

                @Override
                protected void done() {
                    try {
                        lastGeneratedAudioPath = get();
                        playButton.setEnabled(true);
                        JOptionPane.showMessageDialog(frame, "Ses dosyası başarıyla oluşturuldu:\n" + lastGeneratedAudioPath.getFileName(), "Başarılı", JOptionPane.INFORMATION_MESSAGE);
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(frame, "Hata oluştu: " + e.getCause().getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        generateButton.setEnabled(true);
                        generateButton.setText("Sesi Oluştur");
                    }
                }
            };
            worker.execute();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(frame, "Ses ayarları geçersiz. Lütfen ayarları kontrol edin.", "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void playSound() {
        if (lastGeneratedAudioPath != null) {
            try {
                audioFileManager.play(lastGeneratedAudioPath);
            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Ses dosyası oynatılırken hata oluştu: " + e.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}