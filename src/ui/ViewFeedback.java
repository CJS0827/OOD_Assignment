package ui;

import model.User;
import service.CustomerService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

/**
 * ViewFeedback shows all technician feedback written for the customer's appointments.
 *
 * OOP Concepts:
 * - Encapsulation: data loading is hidden inside CustomerService
 * - Polymorphism: User object passed in could be any role, but we treat it as Customer here
 */
public class ViewFeedback extends JFrame {

    private CustomerService customerService = new CustomerService();

    public ViewFeedback(User user) {

        setTitle("Technician Feedback - " + user.getUsername());
        setSize(750, 420);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        setLocationRelativeTo(null);

        // ── Title ──
        JLabel title = new JLabel("Feedback from Technicians");
        title.setFont(new Font("Arial", Font.BOLD, 16));
        title.setBounds(250, 15, 280, 30);
        add(title);

        // ── Back button ──
        JButton btnBack = new JButton("< Back");
        btnBack.setBounds(15, 15, 80, 28);
        add(btnBack);

        // ── Table ──
        String[] columns = {"Feedback ID", "Appointment ID", "Technician", "Feedback Comment", "Date"};
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        JTable table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        table.setRowHeight(24);

        // Allow comment column to wrap by setting a larger preferred width
        int[] colWidths = {90, 110, 100, 280, 90};
        for (int i = 0; i < colWidths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
        }

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBounds(15, 60, 710, 270);
        add(scrollPane);

        // ── Detail panel: show full comment when row clicked ──
        JLabel lblDetail = new JLabel("Click a row to read the full feedback:");
        lblDetail.setBounds(15, 340, 280, 20);
        lblDetail.setForeground(Color.DARK_GRAY);
        add(lblDetail);

        JTextArea detailArea = new JTextArea();
        detailArea.setEditable(false);
        detailArea.setLineWrap(true);
        detailArea.setWrapStyleWord(true);
        detailArea.setBackground(new Color(245, 245, 245));
        detailArea.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        JScrollPane detailScroll = new JScrollPane(detailArea);
        detailScroll.setBounds(15, 360, 710, 0); // hidden until row selected
        // We'll show the detail inside the same panel — use a bottom area
        // Resize frame to fit
        setSize(750, 500);
        detailScroll.setBounds(15, 355, 710, 60);
        add(detailScroll);

        // ── Load data ──
        ArrayList<String[]> feedbacks = customerService.getFeedbacksByCustomer(user.getId());

        if (feedbacks.isEmpty()) {
            JLabel noData = new JLabel("No technician feedback available for your appointments yet.");
            noData.setForeground(Color.GRAY);
            noData.setBounds(180, 185, 400, 25);
            add(noData);
        } else {
            for (String[] fb : feedbacks) {
                // fb: [FeedbackID, AppointmentID, TechnicianID, Comment, Date]
                String techName = customerService.getUsernameById(fb[2]);
                tableModel.addRow(new Object[]{
                    fb[0],     // Feedback ID
                    fb[1],     // Appointment ID
                    techName,  // Technician name
                    fb[3],     // Comment
                    fb[4]      // Date
                });
            }
        }

        // ── Row click → show full comment ──
        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                String techName  = table.getValueAt(row, 2).toString();
                String comment   = table.getValueAt(row, 3).toString();
                String date      = table.getValueAt(row, 4).toString();
                detailArea.setText("[" + date + "] " + techName + ": " + comment);
            }
        });

        // ── Back action ──
        btnBack.addActionListener(e -> {
            dispose();
            new CustomerMenu(user);
        });

        setVisible(true);
    }
}
