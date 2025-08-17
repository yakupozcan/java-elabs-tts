package elevenlabsApp.view;

import elevenlabsApp.config.ApiKeysManager;
import elevenlabsApp.config.SettingsManager;
import elevenlabsApp.service.ElevenLabsService;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class SettingsDialog extends JDialog {

    private final SettingsManager settingsManager;
    private final ApiKeysManager apiKeysManager;
    private final ElevenLabsService elevenLabsService;

    private JComboBox<String> apiKeyComboBox;
    private JComboBox<ElevenLabsService.Voice> voiceComboBox;
    private JTextField voiceIdField;
    private JSlider stabilitySlider;
    private JSlider similaritySlider;
    private JSlider styleSlider;
    private JCheckBox speakerBoostCheckBox;

    private JButton addApiKeyButton;
    private JButton removeApiKeyButton;

    public SettingsDialog(Frame owner, SettingsManager settingsManager, ApiKeysManager apiKeysManager, ElevenLabsService elevenLabsService) {
        super(owner, "Ayarlar", true);
        this.settingsManager = settingsManager;
        this.apiKeysManager = apiKeysManager;
        this.elevenLabsService = elevenLabsService;

        setSize(500, 600);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        mainPanel.add(createApiPanel());
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(createVoiceSelectionPanel());
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(createVoiceSettingsPanel());

        add(mainPanel, BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);

        loadSettings();
    }

    private JPanel createApiPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("API Anahtarı Yönetimi"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("API Anahtarı:"), gbc);

        apiKeyComboBox = new JComboBox<>();
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        panel.add(apiKeyComboBox, gbc);

        addApiKeyButton = new JButton("Ekle");
        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0;
        panel.add(addApiKeyButton, gbc);

        removeApiKeyButton = new JButton("Sil");
        gbc.gridx = 3; gbc.gridy = 0;
        panel.add(removeApiKeyButton, gbc);

        loadApiKeys();

        apiKeyComboBox.addActionListener(e -> onApiKeySelected());
        addApiKeyButton.addActionListener(e -> addApiKey());
        removeApiKeyButton.addActionListener(e -> removeApiKey());

        return panel;
    }

    private JPanel createVoiceSelectionPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Ses Seçimi"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Ses:"), gbc);

        voiceComboBox = new JComboBox<>();
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        panel.add(voiceComboBox, gbc);

        voiceComboBox.addActionListener(e -> {
            ElevenLabsService.Voice selectedVoice = (ElevenLabsService.Voice) voiceComboBox.getSelectedItem();
            if (selectedVoice != null && selectedVoice.voiceId != null) {
                voiceIdField.setText(selectedVoice.voiceId);
            } else {
                voiceIdField.setText("");
            }
        });

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Voice ID:"), gbc);

        voiceIdField = new JTextField();
        voiceIdField.setEditable(false);
        voiceIdField.setFont(new Font("Monospaced", Font.PLAIN, 12));
        gbc.gridx = 1; gbc.gridy = 1;
        panel.add(voiceIdField, gbc);

        return panel;
    }

    private JPanel createVoiceSettingsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Detaylı Ses Ayarları"));

        stabilitySlider = createSlider("Kararlılık (Stability)");
        similaritySlider = createSlider("Benzerlik (Similarity Boost)");
        styleSlider = createSlider("Stil (Style Exaggeration)");
        speakerBoostCheckBox = new JCheckBox("Konuşmacı Güçlendirmesi (Speaker Boost)");

        panel.add(stabilitySlider.getParent());
        panel.add(similaritySlider.getParent());
        panel.add(styleSlider.getParent());
        panel.add(speakerBoostCheckBox);

        return panel;
    }

    private JSlider createSlider(String title) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.add(new JLabel(title), BorderLayout.NORTH);
        JSlider slider = new JSlider(0, 100);
        slider.setMajorTickSpacing(25);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        panel.add(slider, BorderLayout.CENTER);
        return slider;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton defaultsButton = new JButton("Varsayılanlar");
        JButton saveButton = new JButton("Kaydet");
        JButton cancelButton = new JButton("İptal");

        defaultsButton.addActionListener(e -> loadDefaultSettings());

        saveButton.addActionListener(e -> {
            saveSettings();
            setVisible(false);
            dispose();
        });
        cancelButton.addActionListener(e -> {
            setVisible(false);
            dispose();
        });

        panel.add(defaultsButton);
        panel.add(saveButton);
        panel.add(cancelButton);
        return panel;
    }

    private void loadApiKeys() {
        apiKeyComboBox.removeAllItems();
        for (String keyName : apiKeysManager.getApiKeyNames()) {
            apiKeyComboBox.addItem(keyName);
        }
    }

    private void addApiKey() {
        String keyName = JOptionPane.showInputDialog(this, "API Key için bir isim girin:", "API Key Ekle", JOptionPane.PLAIN_MESSAGE);
        if (keyName != null && !keyName.trim().isEmpty()) {
            String keyValue = JOptionPane.showInputDialog(this, keyName + " için API Key değerini girin:", "API Key Ekle", JOptionPane.PLAIN_MESSAGE);
            if (keyValue != null && !keyValue.trim().isEmpty()) {
                try {
                    apiKeysManager.addApiKey(keyName, keyValue);
                    loadApiKeys();
                    apiKeyComboBox.setSelectedItem(keyName);
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(this, "API key kaydedilirken hata oluştu: " + e.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void removeApiKey() {
        String selectedKeyName = (String) apiKeyComboBox.getSelectedItem();
        if (selectedKeyName != null) {
            int confirm = JOptionPane.showConfirmDialog(this, selectedKeyName + " isimli API key'i silmek istediğinizden emin misiniz?", "API Key Sil", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    apiKeysManager.removeApiKey(selectedKeyName);
                    loadApiKeys();
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(this, "API key silinirken hata oluştu: " + e.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void onApiKeySelected() {
        String selectedKeyName = (String) apiKeyComboBox.getSelectedItem();
        if (selectedKeyName == null) return;

        String apiKey = apiKeysManager.getApiKey(selectedKeyName);
        if (apiKey == null) return;

        voiceComboBox.setEnabled(false);
        voiceComboBox.removeAllItems();
        voiceComboBox.addItem(new ElevenLabsService.Voice(null, "Sesler yükleniyor..."));

        SwingWorker<List<ElevenLabsService.Voice>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<ElevenLabsService.Voice> doInBackground() throws Exception {
                return elevenLabsService.getAvailableVoices(apiKey);
            }

            @Override
            protected void done() {
                try {
                    List<ElevenLabsService.Voice> voices = get();
                    voiceComboBox.removeAllItems();
                    for (ElevenLabsService.Voice voice : voices) {
                        voiceComboBox.addItem(voice);
                    }
                    // Try to select the previously saved voice
                    String savedVoiceId = settingsManager.getProperty("selectedVoiceId", null);
                    if (savedVoiceId != null) {
                        for (int i = 0; i < voiceComboBox.getItemCount(); i++) {
                            if (voiceComboBox.getItemAt(i).voiceId.equals(savedVoiceId)) {
                                voiceComboBox.setSelectedIndex(i);
                                break;
                            }
                        }
                    }
                } catch (ExecutionException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(SettingsDialog.this, "Sesler yüklenirken hata oluştu: " + e.getCause().getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
                    voiceComboBox.removeAllItems();
                    voiceComboBox.addItem(new ElevenLabsService.Voice(null, "Hata oluştu"));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    voiceComboBox.setEnabled(true);
                }
            }
        };
        worker.execute();
    }

    private void loadSettings() {
        // Load API Key
        String selectedApiKey = settingsManager.getProperty("selectedApiKeyName", null);
        if (selectedApiKey != null) {
            apiKeyComboBox.setSelectedItem(selectedApiKey);
        }
        // onApiKeySelected will be triggered, which will load voices and the selected voice.

        // Load Voice Settings
        stabilitySlider.setValue((int) (Double.parseDouble(settingsManager.getProperty("stability", "0.75")) * 100));
        similaritySlider.setValue((int) (Double.parseDouble(settingsManager.getProperty("similarityBoost", "0.75")) * 100));
        styleSlider.setValue((int) (Double.parseDouble(settingsManager.getProperty("style", "0.0")) * 100));
        speakerBoostCheckBox.setSelected(Boolean.parseBoolean(settingsManager.getProperty("useSpeakerBoost", "true")));
    }

    private void saveSettings() {
        // Save API Key
        settingsManager.setProperty("selectedApiKeyName", (String) apiKeyComboBox.getSelectedItem());

        // Save Voice
        ElevenLabsService.Voice selectedVoice = (ElevenLabsService.Voice) voiceComboBox.getSelectedItem();
        if (selectedVoice != null && selectedVoice.voiceId != null) {
            settingsManager.setProperty("selectedVoiceId", selectedVoice.voiceId);
        }

        // Save Voice Settings
        settingsManager.setProperty("stability", String.valueOf(stabilitySlider.getValue() / 100.0));
        settingsManager.setProperty("similarityBoost", String.valueOf(similaritySlider.getValue() / 100.0));
        settingsManager.setProperty("style", String.valueOf(styleSlider.getValue() / 100.0));
        settingsManager.setProperty("useSpeakerBoost", String.valueOf(speakerBoostCheckBox.isSelected()));

        settingsManager.save();
    }

    private void loadDefaultSettings() {
        Properties defaultProps = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("default_settings.properties")) {
            if (input == null) {
                JOptionPane.showMessageDialog(this, "Varsayılan ayarlar dosyası bulunamadı.", "Hata", JOptionPane.ERROR_MESSAGE);
                return;
            }
            defaultProps.load(input);

            stabilitySlider.setValue((int) (Double.parseDouble(defaultProps.getProperty("stability", "0.75")) * 100));
            similaritySlider.setValue((int) (Double.parseDouble(defaultProps.getProperty("similarityBoost", "0.75")) * 100));
            styleSlider.setValue((int) (Double.parseDouble(defaultProps.getProperty("style", "0.0")) * 100));
            speakerBoostCheckBox.setSelected(Boolean.parseBoolean(defaultProps.getProperty("useSpeakerBoost", "true")));

        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Varsayılan ayarlar yüklenirken bir hata oluştu.", "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }
}
