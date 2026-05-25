package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import model.User;
import service.TechnicianService;

public class TechFeedbackPanel extends JFrame {

    private TechnicianService techService = new TechnicianService();

    public TechFeedbackPanel(User user) {

        setTitle("Provide Feedback - " + user.getUsername());
        setSize(780, 620);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        setLocationRelativeTo(null);

        JLabel title = new JLabel("Provide Feedback for Appointments");
        title.setFont(new Font("Arial", Font.BOLD, 16));
        title.setBounds(230, 15, 350, 30);
        add(title);

        JButton btnBack = new JButton("< Back");
        btnBack.setBounds(15, 15, 80, 28);
        add(btnBack);

        // Step 1: Completed appointments
        JLabel lblStep1 = new JLabel("Step 1: Select a completed appointment");
        lblStep1.setFont(new Font("Arial", Font.BOLD, 13));
        lblStep1.setBounds(15, 55, 350, 25);
        add(lblStep1);

        String[] cols1 = {"Appt ID", "Customer", "Service Type", "Date", "Car Model", "Plate", "Feedback?"};
        DefaultTableModel completedModel = new DefaultTableModel(cols1, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable completedTable = new JTable(completedModel);
        completedTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        completedTable.getTableHeader().setReorderingAllowed(false);
        completedTable.setRowHeight(24);

        int[] cw = {65, 90, 110, 85, 95, 78, 90};
        for (int i = 0; i < cw.length; i++)
            completedTable.getColumnModel().getColumn(i).setPreferredWidth(cw[i]);

        JScrollPane scroll1 = new JScrollPane(completedTable);
        scroll1.setBounds(15, 83, 740, 140);
        add(scroll1);

        // Step 2: Rating
        JLabel lblStep2 = new JLabel("Step 2: Give a rating (1-5)");
        lblStep2.setFont(new Font("Arial", Font.BOLD, 13));
        lblStep2.setBounds(15, 233, 300, 25);
        add(lblStep2);

        JComboBox<Integer> ratingBox = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5});
        ratingBox.setBounds(15, 258, 80, 25);
        ratingBox.setEnabled(false);
        add(ratingBox);

        // Step 3: Comment
        JLabel lblStep3 = new JLabel("Step 3: Write your feedback comment");
        lblStep3.setFont(new Font("Arial", Font.BOLD, 13));
        lblStep3.setBounds(15, 293, 320, 25);
        add(lblStep3);

        JLabel lblStatus = new JLabel("(Select a completed appointment above first)");
        lblStatus.setForeground(Color.GRAY);
        lblStatus.setBounds(15, 313, 450, 20);
        add(lblStatus);

        JTextArea txtComment = new JTextArea();
        txtComment.setLineWrap(true);
        txtComment.setWrapStyleWord(true);
        txtComment.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        txtComment.setEnabled(false);

        JScrollPane scroll2 = new JScrollPane(txtComment);
        scroll2.setBounds(15, 335, 740, 80);
        add(scroll2);

        JLabel charCount = new JLabel("0 / 300 characters");
        charCount.setForeground(Color.GRAY);
        charCount.setBounds(620, 418, 140, 20);
        add(charCount);

        txtComment.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            void update() {
                int len = txtComment.getText().length();
                charCount.setText(len + " / 300 characters");
                charCount.setForeground(len > 300 ? Color.RED : Color.GRAY);
            }
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { update(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { update(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { update(); }
        });

        JButton btnSubmit = new JButton("Submit Feedback");
        btnSubmit.setBounds(295, 422, 180, 30);
        btnSubmit.setBackground(new Color(51, 153, 255));
        btnSubmit.setForeground(Color.WHITE);
        btnSubmit.setEnabled(false);
        add(btnSubmit);

        // Submitted feedbacks table
        // Format: F001|A001|U003|5|Comment|Date
        JLabel lblPast = new JLabel("My Submitted Feedbacks:");
        lblPast.setFont(new Font("Arial", Font.BOLD, 13));
        lblPast.setBounds(15, 465, 280, 22);
        add(lblPast);

        String[] cols2 = {"Feedback ID", "Appt ID", "Rating", "Comment", "Date"};
        DefaultTableModel submittedModel = new DefaultTableModel(cols2, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable submittedTable = new JTable(submittedModel);
        submittedTable.setRowHeight(24);
        submittedTable.getTableHeader().setReorderingAllowed(false);

        int[] sw = {90, 75, 60, 400, 90};
        for (int i = 0; i < sw.length; i++)
            submittedTable.getColumnModel().getColumn(i).setPreferredWidth(sw[i]);

        JScrollPane scroll3 = new JScrollPane(submittedTable);
        scroll3.setBounds(15, 490, 740, 90);
        add(scroll3);

        // Load completed appointments
        ArrayList<String[]> appointments = techService.getAppointmentsByTechnician(user.getId());
        for (String[] a : appointments) {
            if (a.length < 10 || !a[9].equalsIgnoreCase("Completed")) continue;
            String customer = techService.getUsernameById(a[1]);
            boolean done    = techService.hasFeedback(a[0], user.getId());
            completedModel.addRow(new Object[]{
                a[0], customer, a[2], a[4], a[8], a[7], done ? "Yes" : "No"
            });
        }

        if (completedModel.getRowCount() == 0) {
            JLabel noData = new JLabel("No completed appointments yet.");
            noData.setForeground(Color.GRAY);
            noData.setBounds(130, 148, 500, 22);
            add(noData);
        }

        // Load submitted feedbacks
        // Format: F001|A001|U003|5|Comment|Date
        for (String[] f : techService.getFeedbacksByTechnician(user.getId())) {
            if (f.length >= 6) {
                submittedModel.addRow(new Object[]{
                    f[0],  // Feedback ID
                    f[1],  // Appt ID
                    f[3],  // Rating
                    f[4],  // Comment
                    f[5]   // Date
                });
            }
        }

        // Row selection → enable form
        completedTable.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int row = completedTable.getSelectedRow();
            if (row < 0) return;
            String apptID = completedModel.getValueAt(row, 0).toString();
            boolean done  = completedModel.getValueAt(row, 6).toString().equalsIgnoreCase("Yes");
            txtComment.setText("");
            ratingBox.setSelectedIndex(0);
            if (done) {
                lblStatus.setText("Feedback already submitted for appointment " + apptID + ".");
                lblStatus.setForeground(new Color(0, 120, 0));
                btnSubmit.setEnabled(false);
                txtComment.setEnabled(false);
                ratingBox.setEnabled(false);
            } else {
                lblStatus.setText("Selected: " + apptID + " — fill in rating and comment below.");
                lblStatus.setForeground(Color.DARK_GRAY);
                btnSubmit.setEnabled(true);
                txtComment.setEnabled(true);
                ratingBox.setEnabled(true);
            }
        });

        // Submit
        btnSubmit.addActionListener(e -> {
            int row = completedTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Please select an appointment first.");
                return;
            }
            String apptID  = completedModel.getValueAt(row, 0).toString();
            String comment = txtComment.getText().trim();
            int rating     = (int) ratingBox.getSelectedItem();

            if (comment.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please write a feedback comment.");
                return;
            }
            if (comment.length() > 300) {
                JOptionPane.showMessageDialog(this, "Feedback must be 300 characters or less.");
                return;
            }
            if (comment.contains("|")) {
                JOptionPane.showMessageDialog(this, "Feedback must not contain the '|' character.");
                return;
            }
            if (techService.hasFeedback(apptID, user.getId())) {
                JOptionPane.showMessageDialog(this, "Feedback already submitted for this appointment.");
                dispose();
                new TechFeedbackPanel(user);
                return;
            }

            // saveFeedback now includes rating
            if (techService.saveFeedback(apptID, user.getId(), rating, comment, LocalDate.now().toString())) {
                JOptionPane.showMessageDialog(this, "Feedback submitted successfully!");
                dispose();
                new TechFeedbackPanel(user);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to save feedback. Please try again.");
            }
        });

        btnBack.addActionListener(e -> {
            dispose();
            new TechnicianMenu(user);
        });

        setVisible(true);
    }
}