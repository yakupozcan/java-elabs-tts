
package elevenlabsApp;


import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Font;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

public class Main {

    private static JTextField textField;
    private static final String API_KEY = "YOUR_ELEVENLABS_API_KEY";


    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }

    private static void createAndShowGUI() {

        JFrame frame = new JFrame("ElevenLabs Text-to-Speech");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 150);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel label = new JLabel("Metni Girin:");
        label.setFont(new Font("Arial", Font.BOLD, 14));

        textField = new JTextField();
        textField.setFont(new Font("Arial", Font.PLAIN, 14));

        JButton anonsButonu = new JButton("Anons Yap");
        anonsButonu.setFont(new Font("Arial", Font.BOLD, 14));

        panel.add(label, BorderLayout.NORTH);
        panel.add(textField, BorderLayout.CENTER);
        panel.add(anonsButonu, BorderLayout.SOUTH);

        frame.getContentPane().add(panel);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);


        anonsButonu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String text = textField.getText();
                if (text.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Metin alanı boş olamaz.", "Hata", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (API_KEY.equals("YOUR_ELEVENLABS_API_KEY")) {
                    JOptionPane.showMessageDialog(null, "Lütfen API anahtarınızı girin.", "Hata", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                new Thread(() -> {
                    try {
                        ElevenLabsAPI elevenLabsAPI = new ElevenLabsAPI();
                        InputStream audioStream = elevenLabsAPI.textToSpeech(text, API_KEY);
                        play(audioStream);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(null, "API isteği sırasında bir hata oluştu.", "Hata", JOptionPane.ERROR_MESSAGE);
                    }
                }).start();
            }
        });
    }

    public static void play(InputStream inputStream) {
        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(inputStream);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }

    }
}