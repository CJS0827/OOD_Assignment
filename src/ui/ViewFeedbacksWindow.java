package ui;

import model.User;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.*;
import java.util.ArrayList;

public class ViewFeedbacksWindow {

    private final String FEEDBACK_FILE = "data/Feedbacks.txt";
    private final String COMMENTS_FILE = "data/Comments.txt";
    private final String USERS_FILE    = "data/users.txt";

    public ViewFeedbacksWindow(User manager) {

        JFrame frame = new JFrame("View Feedbacks & Comments");
        frame.setSize(900, 520);
        frame.setLayout(null);
        frame.setLocationRelativeTo(null);

        JButton btnBack = new JButton("< Back");
        btnBack.setBounds(30, 10, 90, 25);
        frame.add(btnBack);

        JLabel title = new JLabel("Customer Feedbacks & Staff Comments");
        title.setBounds(290, 10, 350, 25);
        title.setFont(title.getFont().deriveFont(14.0f));
        frame.add(title);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setBounds(20, 45, 845, 420);
        frame.add(tabs);

        tabs.addTab("Feedbacks", buildFeedbackTab());
        tabs.addTab("Comments",  buildCommentTab());

        btnBack.addActionListener(e -> {
            frame.dispose();
            new ManagerMenu(manager);
        });

        frame.setVisible(true);
    }

    private JPanel buildFeedbackTab() {
        JPanel panel = new JPanel(null);

        JLabel lblFilter = new JLabel("Filter by:");
        lblFilter.setBounds(10, 10, 60, 25);
        panel.add(lblFilter);

        JTextField tfFilter = new JTextField();
        tfFilter.setBounds(75, 10, 180, 25);
        panel.add(tfFilter);

        JButton btnFilter  = new JButton("Filter");
        JButton btnShowAll = new JButton("Show All");
        btnFilter.setBounds(265, 10, 80, 25);
        btnShowAll.setBounds(355, 10, 90, 25);
        panel.add(btnFilter);
        panel.add(btnShowAll);

        // Format: F001|A001|U003|5|Great service|2026-04-10
        DefaultTableModel model = new DefaultTableModel();
        model.setColumnIdentifiers(new String[]{
            "Feedback ID", "Appt ID", "Technician", "Rating", "Comment", "Date"
        });

        JTable table = new JTable(model);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBounds(10, 45, 815, 330);
        panel.add(scroll);

        ArrayList<String[]> all = loadFile(FEEDBACK_FILE, 6);

        // Initial load
        model.setRowCount(0);
        for (String[] row : all)
            model.addRow(new Object[]{
                row[0],
                row[1],
                resolveUserName(row[2]),
                row[3],
                row[4],
                row[5]
            });

        btnFilter.addActionListener(e -> {
            String kw = tfFilter.getText().trim().toLowerCase();
            if (kw.isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Enter a keyword.");
                return;
            }
            model.setRowCount(0);
            for (String[] row : all)
                if (row[0].toLowerCase().contains(kw) ||
                    row[1].toLowerCase().contains(kw))
                    model.addRow(new Object[]{
                        row[0], row[1], resolveUserName(row[2]), row[3], row[4], row[5]
                    });
        });

        btnShowAll.addActionListener(e -> {
            tfFilter.setText("");
            model.setRowCount(0);
            for (String[] row : all)
                model.addRow(new Object[]{
                    row[0], row[1], resolveUserName(row[2]), row[3], row[4], row[5]
                });
        });

        return panel;
    }

    private JPanel buildCommentTab() {
        JPanel panel = new JPanel(null);

        JLabel lblFilter = new JLabel("Filter by:");
        lblFilter.setBounds(10, 10, 60, 25);
        panel.add(lblFilter);

        JTextField tfFilter = new JTextField();
        tfFilter.setBounds(75, 10, 180, 25);
        panel.add(tfFilter);

        JButton btnFilter  = new JButton("Filter");
        JButton btnShowAll = new JButton("Show All");
        btnFilter.setBounds(265, 10, 80, 25);
        btnShowAll.setBounds(355, 10, 90, 25);
        panel.add(btnFilter);
        panel.add(btnShowAll);

        // Format: C001|A001|U005|U002|CounterStaff|comment text|date
        DefaultTableModel model = new DefaultTableModel();
        model.setColumnIdentifiers(new String[]{
            "Comment ID", "Appt ID", "Customer", "Recipient", "Role", "Comment", "Date"
        });

        JTable table = new JTable(model);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBounds(10, 45, 815, 330);
        panel.add(scroll);

        ArrayList<String[]> all = loadFile(COMMENTS_FILE, 7);
        displayComments(model, all);

        btnFilter.addActionListener(e -> {
            String kw = tfFilter.getText().trim().toLowerCase();
            if (kw.isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Enter a keyword.");
                return;
            }
            ArrayList<String[]> filtered = new ArrayList<>();
            for (String[] row : all)
                if (row[1].toLowerCase().contains(kw) ||  // Appt ID
                    row[4].toLowerCase().contains(kw))     // Role
                    filtered.add(row);
            displayComments(model, filtered);
        });

        btnShowAll.addActionListener(e -> {
            tfFilter.setText("");
            displayComments(model, all);
        });

        return panel;
    }

    private ArrayList<String[]> loadFile(String path, int minCols) {
        ArrayList<String[]> list = new ArrayList<>();
        File file = new File(path);
        if (!file.exists()) return list;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split("\\|");
                if (data.length >= minCols) list.add(data);
            }
        } catch (IOException e) {
            System.out.println("Error reading " + path + ": " + e.getMessage());
        }
        return list;
    }

    private void displayComments(DefaultTableModel model, ArrayList<String[]> rows) {
        model.setRowCount(0);
        for (String[] row : rows) {
            model.addRow(new Object[]{
                row[0],                  // Comment ID
                row[1],                  // Appt ID
                resolveUserName(row[2]), // Customer name
                resolveUserName(row[3]), // Recipient name
                row[4],                  // Role
                row[5],                  // Comment text
                row[6]                   // Date
            });
        }
    }

    private String resolveUserName(String userId) {
        File file = new File(USERS_FILE);
        if (!file.exists()) return userId;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split("\\|");
                if (data.length >= 2 && data[0].equalsIgnoreCase(userId))
                    return data[1];
            }
        } catch (IOException e) {}
        return userId;
    }
}