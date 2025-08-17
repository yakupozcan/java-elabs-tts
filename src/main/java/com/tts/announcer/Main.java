package com.tts.announcer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Anons Sistemi");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout(10, 10));

        JTextField anonsMetni = new JTextField();
        anonsMetni.setFont(new Font("Arial", Font.PLAIN, 16));

        JButton anonsButonu = new JButton("Sesi Oluştur");
        anonsButonu.setFont(new Font("Arial", Font.BOLD, 18));

        anonsButonu.addActionListener(e -> {
            String text = anonsMetni.getText();
            if (text != null && !text.trim().isEmpty()) {
                System.out.println("Butona basıldı! İşlenecek metin: '" + text + "'");
                // SONRAKİ GÖREV: Bu kısımda API çağrılacak.
            } else {
                System.out.println("Metin alanı boş, işlem yapılmadı.");
            }
        });

        panel.add(anonsMetni, BorderLayout.CENTER);
        panel.add(anonsButonu, BorderLayout.SOUTH);

        frame.getContentPane().add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
