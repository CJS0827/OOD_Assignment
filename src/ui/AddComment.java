package ui;

import model.User;
import service.CustomerService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import java.awt.*;
import java.util.ArrayList;

public class AddComment extends JFrame {

    private CustomerService customerService = new CustomerService();

    private String selectedApptID    = null;
    private String selectedTargetID  = null;
    private String selectedTargetRole = null;

    public AddComment(User user) {

        setTitle("Add Comment - " + user.getUsername());
        setSize(780, 580);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        setLocationRelativeTo(null);

        JLabel title = new JLabel("Provide Comments");
        title.setFont(new Font("Arial", Font.BOLD, 16));
        title.setBounds(310, 15, 200, 30);
        add(title);

        JButton btnBack = new JButton("< Back");
        btnBack.setBounds(15, 15, 80, 28);
        add(btnBack);

        // ── Step 1: Select Appointment ──
        JLabel lblStep1 = new JLabel("Step 1: Select your completed appointment");
        lblStep1.setFont(new Font("Arial", Font.BOLD, 13));
        lblStep1.setBounds(15, 55, 350, 25);
        add(lblStep1);

        String[] apptCols = {"Appt ID", "Service Type", "Technician", "Date", "Status"};
        DefaultTableModel apptModel = new DefaultTableModel(apptCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable apptTable = new JTable(apptModel);
        apptTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        apptTable.setRowHeight(24);

        // Sortable — click any header to sort
        TableRowSorter<DefaultTableModel> apptSorter = new TableRowSorter<>(apptModel);
        apptTable.setRowSorter(apptSorter);

        // Default: Appt ID (col 0) descending
        apptSorter.setSortKeys(java.util.List.of(
            new RowSorter.SortKey(0, SortOrder.DESCENDING)
        ));

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

        JLabel lblStep2note = new JLabel("(Select a completed appointment above first)");
        lblStep2note.setForeground(Color.GRAY);
        lblStep2note.setBounds(15, 245, 400, 20);
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
        commentArea.setEnabled(false);

        JScrollPane commentScroll = new JScrollPane(commentArea);
        commentScroll.setBounds(15, 405, 740, 80);
        add(commentScroll);

        JLabel charCount = new JLabel("0 / 200 characters");
        charCount.setForeground(Color.GRAY);
        charCount.setBounds(630, 490, 130, 20);
        add(charCount);

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

        JButton btnSubmit = new JButton("Submit Comment");
        btnSubmit.setBounds(295, 495, 180, 32);
        btnSubmit.setBackground(new Color(51, 153, 255));
        btnSubmit.setForeground(Color.WHITE);
        btnSubmit.setEnabled(false);
        add(btnSubmit);

        // ── Load appointments ──
        ArrayList<String[]> appointments = customerService.getAppointmentsByCustomer(user.getId());
        for (String[] appt : appointments) {
            String techName = customerService.getUsernameById(appt[3]);
            apptModel.addRow(new Object[]{
                appt[0], appt[2], techName, appt[4], appt[9]
            });
        }

        // ── Appointment row selected ──
        apptTable.getSelectionModel().addListSelectionListener(e -> {
            int viewRow = apptTable.getSelectedRow();
            if (viewRow < 0) return;

            // Convert view index to model index (important when sorted)
            int modelRow = apptTable.convertRowIndexToModel(viewRow);

            selectedApptID = apptModel.getValueAt(modelRow, 0).toString();
            String[] appt  = appointments.get(modelRow);
            String status  = appt[9];

            targetModel.setRowCount(0);
            selectedTargetID   = null;
            selectedTargetRole = null;
            commentArea.setText("");

            // Only allow commenting on Completed appointments
            if (!status.equalsIgnoreCase("Completed")) {
                lblStep2note.setText("⚠ Comments only allowed for Completed appointments.");
                lblStep2note.setForeground(new Color(180, 80, 0));
                commentArea.setEnabled(false);
                btnSubmit.setEnabled(false);
                return;
            }

            lblStep2note.setText("Select who to comment on below.");
            lblStep2note.setForeground(Color.DARK_GRAY);
            commentArea.setEnabled(true);
            btnSubmit.setEnabled(true);

            // Add Technician row
            String techID   = appt[3];
            String techName = customerService.getUsernameById(techID);
            boolean techCommented = customerService.hasCommented(selectedApptID, user.getId(), techID);
            targetModel.addRow(new Object[]{
                techID, techName, "Technician",
                techCommented ? "✓ Yes" : "Not yet"
            });

            // Add Counter Staff row
            String[] staff = customerService.getCounterStaffForAppointment(selectedApptID);
            if (staff != null) {
                boolean staffCommented = customerService.hasCommented(selectedApptID, user.getId(), staff[0]);
                targetModel.addRow(new Object[]{
                    staff[0], staff[1], "CounterStaff",
                    staffCommented ? "✓ Yes" : "Not yet"
                });
            }
        });

        // ── Target row selected ──
        targetTable.getSelectionModel().addListSelectionListener(e -> {
            int row = targetTable.getSelectedRow();
            if (row < 0) return;
            selectedTargetID   = targetTable.getValueAt(row, 0).toString();
            selectedTargetRole = targetTable.getValueAt(row, 2).toString();
        });

        // ── Submit ──
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

                // Refresh already-commented column
                int viewRow = apptTable.getSelectedRow();
                if (viewRow >= 0) {
                    apptTable.getSelectionModel().clearSelection();
                    apptTable.getSelectionModel().setSelectionInterval(viewRow, viewRow);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Failed to save comment. Please try again.");
            }
        });

        btnBack.addActionListener(e -> {
            dispose();
            new CustomerMenu(user);
        });

        setVisible(true);
    }
}