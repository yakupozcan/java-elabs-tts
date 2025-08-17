
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

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }

    private static void createAndShowGUI() {
        JFrame cerceve = new JFrame("Hastane Anons Sistemi");
        cerceve.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        cerceve.setSize(500, 200);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 10));

        JTextField anonsMetni = new JTextField("Lütfen anons metnini buraya giriniz...");
        anonsMetni.setFont(new Font("Arial", Font.ITALIC, 16));

        JButton anonsButonu = new JButton("Anons Yap");
        anonsButonu.setFont(new Font("Arial", Font.BOLD, 18));

        panel.add(anonsMetni, BorderLayout.CENTER);
        panel.add(anonsButonu, BorderLayout.SOUTH);

        cerceve.getContentPane().add(panel);
        cerceve.setLocationRelativeTo(null);
        cerceve.setVisible(true);

        anonsButonu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String text = anonsMetni.getText();
                if (text != null && !text.trim().isEmpty()) {
                    System.out.println("Butona tıklandı. Gönderilen metin: " + text);
                    ElevenLabsApiService apiService = new ElevenLabsApiService();
                    apiService.sendTextToSpeechRequest(text);
                } else {
                    System.out.println("Metin alanı boş.");
                }
            }
        });
    }
}