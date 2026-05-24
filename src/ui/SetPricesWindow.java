package ui;

import model.User;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

// GUI & Events: Swing components for price management (OODJ Principle)
public class SetPricesWindow {

    // Encapsulation: private file path constant
    private final String PRICES_FILE = "data/prices.txt";

    // Reasonable upper bound to catch fat-finger errors
    private final double MAX_PRICE = 10000.00;

    public SetPricesWindow(User manager) {

        JFrame frame = new JFrame("Set Service Prices");
        frame.setSize(480, 400);
        frame.setLayout(null);
        frame.setLocationRelativeTo(null);

        // Back button
        JButton btnBack = new JButton("< Back");
        btnBack.setBounds(20, 10, 90, 25);
        frame.add(btnBack);

        JLabel title = new JLabel("Set Service Prices");
        title.setBounds(160, 10, 200, 25);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 14f));
        frame.add(title);

        // Info banner explaining what this screen does
        JLabel info = new JLabel("<html><i>Update prices below. Changes apply to new appointments only.</i></html>");
        info.setBounds(20, 45, 440, 25);
        info.setForeground(new Color(90, 90, 90));
        frame.add(info);

        // ===== Normal Service section =====
        JPanel normalPanel = new JPanel(null);
        normalPanel.setBounds(20, 80, 440, 70);
        normalPanel.setBorder(new LineBorder(new Color(200, 200, 200), 1));
        normalPanel.setBackground(new Color(248, 252, 248));
        frame.add(normalPanel);

        JLabel lblNormal = new JLabel("Normal Service (1 hour)");
        lblNormal.setBounds(15, 8, 250, 22);
        lblNormal.setFont(lblNormal.getFont().deriveFont(Font.BOLD, 12f));
        normalPanel.add(lblNormal);

        JLabel lblNormalPrice = new JLabel("Price (RM):");
        lblNormalPrice.setBounds(15, 35, 80, 25);
        normalPanel.add(lblNormalPrice);

        JTextField tfNormalPrice = new JTextField();
        tfNormalPrice.setBounds(100, 35, 120, 25);
        normalPanel.add(tfNormalPrice);

        // ===== Major Service section =====
        JPanel majorPanel = new JPanel(null);
        majorPanel.setBounds(20, 160, 440, 70);
        majorPanel.setBorder(new LineBorder(new Color(200, 200, 200), 1));
        majorPanel.setBackground(new Color(252, 248, 248));
        frame.add(majorPanel);

        JLabel lblMajor = new JLabel("Major Service (3 hours)");
        lblMajor.setBounds(15, 8, 250, 22);
        lblMajor.setFont(lblMajor.getFont().deriveFont(Font.BOLD, 12f));
        majorPanel.add(lblMajor);

        JLabel lblMajorPrice = new JLabel("Price (RM):");
        lblMajorPrice.setBounds(15, 35, 80, 25);
        majorPanel.add(lblMajorPrice);

        JTextField tfMajorPrice = new JTextField();
        tfMajorPrice.setBounds(100, 35, 120, 25);
        majorPanel.add(tfMajorPrice);

        // ===== Buttons =====
        JButton btnSave = new JButton("Save Prices");
        btnSave.setBounds(140, 250, 130, 35);
        btnSave.setBackground(new Color(51, 153, 102));
        btnSave.setForeground(Color.WHITE);
        frame.add(btnSave);

        JButton btnCancel = new JButton("Cancel");
        btnCancel.setBounds(280, 250, 120, 35);
        frame.add(btnCancel);

        // Last-modified label at bottom
        JLabel lblLastModified = new JLabel("Last modified: --");
        lblLastModified.setBounds(20, 305, 440, 20);
        lblLastModified.setFont(lblLastModified.getFont().deriveFont(Font.ITALIC, 10f));
        lblLastModified.setForeground(new Color(120, 120, 120));
        frame.add(lblLastModified);

        // Load existing prices + show last-modified timestamp
        loadPrices(tfNormalPrice, tfMajorPrice);
        updateLastModifiedLabel(lblLastModified);

        // ActionListener: Save prices with validation + confirmation
        btnSave.addActionListener(e -> {
            try {
                String normalText = tfNormalPrice.getText().trim();
                String majorText = tfMajorPrice.getText().trim();

                // Empty-field check
                if (normalText.isEmpty() || majorText.isEmpty()) {
                    JOptionPane.showMessageDialog(frame,
                        "Please enter both prices.",
                        "Missing Input", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Exception Handling: NumberFormatException for invalid price input
                double normalPrice = Double.parseDouble(normalText);
                double majorPrice = Double.parseDouble(majorText);

                // Range validation: 0 < price <= MAX_PRICE
                if (normalPrice <= 0 || majorPrice <= 0) {
                    JOptionPane.showMessageDialog(frame,
                        "Prices must be greater than zero.",
                        "Invalid Price", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (normalPrice > MAX_PRICE || majorPrice > MAX_PRICE) {
                    JOptionPane.showMessageDialog(frame,
                        "Prices cannot exceed RM " + String.format("%.2f", MAX_PRICE) + ".\n"
                            + "Please check the values you entered.",
                        "Price Too High", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Business logic check: Major should cost more than Normal
                if (majorPrice < normalPrice) {
                    int choice = JOptionPane.showConfirmDialog(
                        frame,
                        "Major Service price (RM " + String.format("%.2f", majorPrice)
                            + ") is LOWER than Normal Service price (RM "
                            + String.format("%.2f", normalPrice) + ").\n\n"
                            + "This is unusual. Do you still want to save?",
                        "Unusual Pricing",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                    );
                    if (choice != JOptionPane.YES_OPTION) return;
                }

                // Confirmation dialog before persisting changes
                int confirm = JOptionPane.showConfirmDialog(
                    frame,
                    "Save the following prices?\n\n"
                        + "Normal Service:  RM " + String.format("%.2f", normalPrice) + "\n"
                        + "Major Service:   RM " + String.format("%.2f", majorPrice) + "\n\n"
                        + "These prices will apply to all new appointments.",
                    "Confirm Save",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
                );

                if (confirm != JOptionPane.YES_OPTION) {
                    return; // user cancelled
                }

                // File I/O: persist new prices
                savePrices(normalPrice, majorPrice);
                updateLastModifiedLabel(lblLastModified);

                JOptionPane.showMessageDialog(frame,
                    "Prices saved successfully!\n\n"
                        + "Normal Service: RM " + String.format("%.2f", normalPrice) + "\n"
                        + "Major Service: RM " + String.format("%.2f", majorPrice),
                    "Saved", JOptionPane.INFORMATION_MESSAGE);

            } catch (NumberFormatException ex) {
                // Robustness: Handle invalid number input (Lecture 9.0)
                JOptionPane.showMessageDialog(frame,
                    "Invalid price format. Please enter valid numbers (e.g. 50.00).",
                    "Invalid Input", JOptionPane.ERROR_MESSAGE);
            }
        });

        // ActionListener: Cancel → revert text fields to saved values
        btnCancel.addActionListener(e -> {
            // Reload prices from file to discard any unsaved edits
            loadPrices(tfNormalPrice, tfMajorPrice);
            JOptionPane.showMessageDialog(frame, "Changes discarded.");
        });

        // ActionListener: Back to Manager Menu
        btnBack.addActionListener(e -> {
            frame.dispose();
            new ManagerMenu(manager);
        });

        frame.setVisible(true);
    }

    // File I/O: BufferedReader to load prices from file
    private void loadPrices(JTextField tfNormal, JTextField tfMajor) {
        File file = new File(PRICES_FILE);
        if (!file.exists()) {
            // Set default prices if file does not exist
            tfNormal.setText("50.00");
            tfMajor.setText("150.00");
            return;
        }

        // Exception Handling: try-with-resources for BufferedReader
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                // String.split() to parse pipe-delimited data
                String[] data = line.split("\\|");
                if (data.length >= 3) {
                    if (data[0].equalsIgnoreCase("Normal Service")) {
                        tfNormal.setText(data[2]);
                    } else if (data[0].equalsIgnoreCase("Major Service")) {
                        tfMajor.setText(data[2]);
                    }
                }
            }
        } catch (IOException e) {
            // Graceful error handling with defaults
            tfNormal.setText("50.00");
            tfMajor.setText("150.00");
        }
    }

    // File I/O: BufferedWriter/FileWriter to save prices
    private void savePrices(double normalPrice, double majorPrice) {
        File folder = new File("data");
        folder.mkdirs();

        // Exception Handling: try-with-resources for BufferedWriter
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(PRICES_FILE))) {
            bw.write("Normal Service|1|" + String.format("%.2f", normalPrice));
            bw.newLine();
            bw.write("Major Service|3|" + String.format("%.2f", majorPrice));
            bw.newLine();
        } catch (IOException e) {
            System.out.println("Error saving prices: " + e.getMessage());
        }
    }

    // Helper: Read file's last-modified time and display it
    private void updateLastModifiedLabel(JLabel label) {
        File file = new File(PRICES_FILE);
        if (!file.exists()) {
            label.setText("Last modified: never (defaults loaded)");
            return;
        }

        try {
            BasicFileAttributes attrs = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            LocalDateTime modified = LocalDateTime.ofInstant(
                attrs.lastModifiedTime().toInstant(), ZoneId.systemDefault());
            String formatted = modified.format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm:ss"));
            label.setText("Last modified: " + formatted);
        } catch (IOException e) {
            label.setText("Last modified: unknown");
        }
    }
}