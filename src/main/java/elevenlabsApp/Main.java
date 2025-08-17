
package elevenlabsApp;

import elevenlabsApp.config.ApiKeysManager;
import elevenlabsApp.service.AudioFileManager;
import elevenlabsApp.service.ElevenLabsService;

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

    private final ApiKeysManager apiKeysManager;
    private final ElevenLabsService elevenLabsService;
    private final AudioFileManager audioFileManager;

    private JFrame frame;
    private JComboBox<String> apiKeyComboBox;
    private JTextField voiceIdField;
    private JTextArea textArea;
    private JButton generateButton;
    private JButton playButton;
    private JButton addApiKeyButton;
    private JButton removeApiKeyButton;

    private Path lastGeneratedAudioPath;

    public Main() {
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
        frame.setSize(600, 400);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // API Key Panel
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        mainPanel.add(new JLabel("API Key:"), gbc);

        apiKeyComboBox = new JComboBox<>();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        mainPanel.add(apiKeyComboBox, gbc);

        addApiKeyButton = new JButton("Ekle");
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        mainPanel.add(addApiKeyButton, gbc);

        removeApiKeyButton = new JButton("Sil");
        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        mainPanel.add(removeApiKeyButton, gbc);

        // Voice ID Panel
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        mainPanel.add(new JLabel("Voice ID:"), gbc);

        voiceIdField = new JTextField("21m00Tcm4TlvDq8ikWAM"); // Default Rachel
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 4;
        mainPanel.add(voiceIdField, gbc);

        // Text Area
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 5;
        mainPanel.add(new JLabel("Anons Edilecek Metin:"), gbc);

        textArea = new JTextArea(5, 30);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(textArea);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 5;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        mainPanel.add(scrollPane, gbc);

        // Buttons Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        generateButton = new JButton("Sesi Oluştur");
        playButton = new JButton("Oynat");
        playButton.setEnabled(false); // Initially disabled

        buttonPanel.add(generateButton);
        buttonPanel.add(playButton);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0;
        mainPanel.add(buttonPanel, gbc);


        frame.getContentPane().add(mainPanel);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        // Load API keys
        loadApiKeys();

        // Add Action Listeners
        addApiKeyButton.addActionListener(e -> addApiKey());
        removeApiKeyButton.addActionListener(e -> removeApiKey());
        generateButton.addActionListener(e -> generateSound());
        playButton.addActionListener(e -> playSound());
    }

    private void loadApiKeys() {
        apiKeyComboBox.removeAllItems();
        apiKeysManager.getApiKeyNames().forEach(apiKeyComboBox::addItem);
        if (apiKeyComboBox.getItemCount() == 0) {
            addApiKey();
        }
    }

    private void addApiKey() {
        String keyName = JOptionPane.showInputDialog(frame, "API Key için bir isim girin:", "API Key Ekle", JOptionPane.PLAIN_MESSAGE);
        if (keyName != null && !keyName.trim().isEmpty()) {
            String keyValue = JOptionPane.showInputDialog(frame, keyName + " için API Key değerini girin:", "API Key Ekle", JOptionPane.PLAIN_MESSAGE);
            if (keyValue != null && !keyValue.trim().isEmpty()) {
                try {
                    apiKeysManager.addApiKey(keyName, keyValue);
                    loadApiKeys();
                    apiKeyComboBox.setSelectedItem(keyName);
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(frame, "API key kaydedilirken hata oluştu: " + e.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void removeApiKey() {
        String selectedKeyName = (String) apiKeyComboBox.getSelectedItem();
        if (selectedKeyName != null) {
            int confirm = JOptionPane.showConfirmDialog(frame, selectedKeyName + " isimli API key'i silmek istediğinizden emin misiniz?", "API Key Sil", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    apiKeysManager.removeApiKey(selectedKeyName);
                    loadApiKeys();
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(frame, "API key silinirken hata oluştu: " + e.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void generateSound() {
        String selectedKeyName = (String) apiKeyComboBox.getSelectedItem();
        if (selectedKeyName == null) {
            JOptionPane.showMessageDialog(frame, "Lütfen bir API key seçin veya ekleyin.", "Uyarı", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String apiKey = apiKeysManager.getApiKey(selectedKeyName);

        String voiceId = voiceIdField.getText();
        if (voiceId.trim().isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Lütfen bir Voice ID girin.", "Uyarı", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String text = textArea.getText();
        if (text.trim().isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Lütfen anons edilecek bir metin girin.", "Uyarı", JOptionPane.WARNING_MESSAGE);
            return;
        }

        generateButton.setEnabled(false);
        generateButton.setText("Oluşturuluyor...");
        playButton.setEnabled(false);

        SwingWorker<Path, Void> worker = new SwingWorker<Path, Void>() {
            @Override
            protected Path doInBackground() throws Exception {
                InputStream audioStream = elevenLabsService.textToSpeech(apiKey, text, voiceId);

                File runningDir = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile();
                Path outputPath = Paths.get(runningDir.getAbsolutePath(), UUID.randomUUID().toString() + ".wav");

                audioFileManager.saveAudioStream(audioStream, outputPath);
                return outputPath;
            }

            @Override
            protected void done() {
                try {
                    lastGeneratedAudioPath = get();
                    playButton.setEnabled(true);
                    JOptionPane.showMessageDialog(frame, "Ses dosyası başarıyla oluşturuldu.", "Başarılı", JOptionPane.INFORMATION_MESSAGE);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                    Throwable cause = e.getCause();
                    JOptionPane.showMessageDialog(frame, "Hata oluştu: " + cause.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    generateButton.setEnabled(true);
                    generateButton.setText("Sesi Oluştur");
                }
            }
        };
        worker.execute();
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