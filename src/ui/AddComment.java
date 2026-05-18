package ui;

import model.User;
import service.CustomerService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

/**
 * AddComment allows a customer to leave comments on counter staff and technicians
 * involved in their completed appointments.
 *
 * OOP Concepts:
 * - Encapsulation: saveComment() and data loading hidden inside CustomerService
 * - Polymorphism: User object is passed polymorphically from CustomerMenu
 * - Abstraction: Customer interacts with a clean UI without knowing file details
 */
public class AddComment extends JFrame {

    private CustomerService customerService = new CustomerService();

    // Currently selected appointment and target
    private String selectedApptID   = null;
    private String selectedTargetID  = null;
    private String selectedTargetRole = null;

    public AddComment(User user) {

        setTitle("Add Comment - " + user.getUsername());
        setSize(780, 580);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        setLocationRelativeTo(null);

        // ── Title ──
        JLabel title = new JLabel("Provide Comments");
        title.setFont(new Font("Arial", Font.BOLD, 16));
        title.setBounds(310, 15, 200, 30);
        add(title);

        // ── Back button ──
        JButton btnBack = new JButton("< Back");
        btnBack.setBounds(15, 15, 80, 28);
        add(btnBack);

        // ── Step 1: Select Appointment ──
        JLabel lblStep1 = new JLabel("Step 1: Select your appointment");
        lblStep1.setFont(new Font("Arial", Font.BOLD, 13));
        lblStep1.setBounds(15, 55, 300, 25);
        add(lblStep1);

        String[] apptCols = {"Appt ID", "Service Type", "Technician", "Date", "Status"};
        DefaultTableModel apptModel = new DefaultTableModel(apptCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable apptTable = new JTable(apptModel);
        apptTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        apptTable.getTableHeader().setReorderingAllowed(false);

        int[] apptWidths = {70, 120, 100, 90, 90};
        for (int i = 0; i < apptWidths.length; i++)
            apptTable.getColumnModel().getColumn(i).setPreferredWidth(apptWidths[i]);

        JScrollPane apptScroll = new JScrollPane(apptTable);
        apptScroll.setBounds(15, 85, 740, 130);
        add(apptScroll);

        // ── Step 2: Select who to comment on ──
        JLabel lblStep2 = new JLabel("Step 2: Select who to comment on");
        lblStep2.setFont(new Font("Arial", Font.BOLD, 13));
        lblStep2.setBounds(15, 225, 300, 25);
        add(lblStep2);

        JLabel lblStep2note = new JLabel("(Select an appointment above first)");
        lblStep2note.setForeground(Color.GRAY);
        lblStep2note.setBounds(15, 245, 300, 20);
        add(lblStep2note);

        String[] targetCols = {"User ID", "Name", "Role", "Already Commented?"};
        DefaultTableModel targetModel = new DefaultTableModel(targetCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable targetTable = new JTable(targetModel);
        targetTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        targetTable.getTableHeader().setReorderingAllowed(false);

        int[] tgtWidths = {80, 120, 110, 150};
        for (int i = 0; i < tgtWidths.length; i++)
            targetTable.getColumnModel().getColumn(i).setPreferredWidth(tgtWidths[i]);

        JScrollPane targetScroll = new JScrollPane(targetTable);
        targetScroll.setBounds(15, 268, 740, 100);
        add(targetScroll);

        // ── Step 3: Write Comment ──
        JLabel lblStep3 = new JLabel("Step 3: Write your comment");
        lblStep3.setFont(new Font("Arial", Font.BOLD, 13));
        lblStep3.setBounds(15, 378, 250, 25);
        add(lblStep3);

        JTextArea commentArea = new JTextArea();
        commentArea.setLineWrap(true);
        commentArea.setWrapStyleWord(true);
        commentArea.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        JScrollPane commentScroll = new JScrollPane(commentArea);
        commentScroll.setBounds(15, 405, 740, 80);
        add(commentScroll);

        JLabel charCount = new JLabel("0 / 200 characters");
        charCount.setForeground(Color.GRAY);
        charCount.setBounds(630, 490, 130, 20);
        add(charCount);

        // Character counter
        commentArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            void update() {
                int len = commentArea.getText().length();
                charCount.setText(len + " / 200 characters");
                charCount.setForeground(len > 200 ? Color.RED : Color.GRAY);
            }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { update(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { update(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { update(); }
        });

        // ── Submit button ──
        JButton btnSubmit = new JButton("Submit Comment");
        btnSubmit.setBounds(295, 495, 180, 32);
        btnSubmit.setBackground(new Color(51, 153, 255));
        btnSubmit.setForeground(Color.WHITE);
        add(btnSubmit);

        // ── Load appointments ──
        ArrayList<String[]> appointments = customerService.getAppointmentsByCustomer(user.getId());
        for (String[] appt : appointments) {
            String techName = customerService.getUsernameById(appt[3]);
            apptModel.addRow(new Object[]{
                appt[0], appt[2], techName, appt[4], appt[9]
            });
        }

        // ── When appointment row selected → populate targets ──
        apptTable.getSelectionModel().addListSelectionListener(e -> {
            int row = apptTable.getSelectedRow();
            if (row < 0) return;

            selectedApptID = apptTable.getValueAt(row, 0).toString();
            String[] appt  = appointments.get(row);

            targetModel.setRowCount(0); // clear
            selectedTargetID   = null;
            selectedTargetRole = null;
            commentArea.setText("");

            // Add Technician row
            String techID   = appt[3];
            String techName = customerService.getUsernameById(techID);
            boolean techCommented = customerService.hasCommented(selectedApptID, user.getId(), techID);
            targetModel.addRow(new Object[]{
                techID, techName, "Technician",
                techCommented ? "✓ Yes" : "Not yet"
            });

            // Add the specific Counter Staff who created this appointment
            String[] staff = customerService.getCounterStaffForAppointment(selectedApptID);
            if (staff != null) {
                boolean staffCommented = customerService.hasCommented(selectedApptID, user.getId(), staff[0]);
                targetModel.addRow(new Object[]{
                    staff[0], staff[1], "CounterStaff",
                    staffCommented ? "✓ Yes" : "Not yet"
                });
            }
        });

        // ── When target row selected → remember who to comment on ──
        targetTable.getSelectionModel().addListSelectionListener(e -> {
            int row = targetTable.getSelectedRow();
            if (row < 0) return;
            selectedTargetID   = targetTable.getValueAt(row, 0).toString();
            selectedTargetRole = targetTable.getValueAt(row, 2).toString();
        });

        // ── Submit comment ──
        btnSubmit.addActionListener(e -> {
            if (selectedApptID == null) {
                JOptionPane.showMessageDialog(this, "Please select an appointment first.");
                return;
            }
            if (selectedTargetID == null) {
                JOptionPane.showMessageDialog(this, "Please select who to comment on.");
                return;
            }
            String comment = commentArea.getText().trim();
            if (comment.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please write a comment.");
                return;
            }
            if (comment.length() > 200) {
                JOptionPane.showMessageDialog(this, "Comment must be 200 characters or less.");
                return;
            }
            // Check duplicate
            if (customerService.hasCommented(selectedApptID, user.getId(), selectedTargetID)) {
                JOptionPane.showMessageDialog(this,
                    "You have already commented on this person for this appointment.");
                return;
            }

            boolean saved = customerService.saveComment(
                selectedApptID, user.getId(), selectedTargetID, selectedTargetRole, comment
            );

            if (saved) {
                JOptionPane.showMessageDialog(this, "Comment submitted successfully!");
                commentArea.setText("");

                // Refresh the already-commented column
                int apptRow = apptTable.getSelectedRow();
                if (apptRow >= 0) {
                    // Re-trigger by re-firing selection
                    apptTable.getSelectionModel().clearSelection();
                    apptTable.getSelectionModel().setSelectionInterval(apptRow, apptRow);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Failed to save comment. Please try again.");
            }
        });

        // ── Back ──
        btnBack.addActionListener(e -> {
            dispose();
            new CustomerMenu(user);
        });

        setVisible(true);
    }
}
