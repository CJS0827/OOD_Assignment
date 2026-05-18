package ui;

import model.User;
import service.CustomerService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.ArrayList;

/**
 * PaymentHistory shows all payment records for the logged-in customer,
 * including a running total at the bottom.
 *
 * OOP Concepts:
 * - Encapsulation: CustomerService handles all file reading privately
 * - Inheritance: Extends JFrame (Java's built-in inheritance)
 */
public class PaymentHistory extends JFrame {

    private CustomerService customerService = new CustomerService();

    public PaymentHistory(User user) {

        setTitle("Payment History - " + user.getUsername());
        setSize(720, 430);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        setLocationRelativeTo(null);

        // ── Title ──
        JLabel title = new JLabel("My Payment History");
        title.setFont(new Font("Arial", Font.BOLD, 16));
        title.setBounds(265, 15, 250, 30);
        add(title);

        // ── Back button ──
        JButton btnBack = new JButton("< Back");
        btnBack.setBounds(15, 15, 80, 28);
        add(btnBack);

        // ── Table setup ──
        String[] columns = {"Payment ID", "Appt ID", "Service Type", "Amount (RM)", "Date", "Method"};
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false; // read-only
            }
        };

        JTable table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        table.setRowHeight(24);

        // Right-align the Amount column
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        table.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);

        // Set column widths
        int[] colWidths = {90, 80, 130, 110, 100, 90};
        for (int i = 0; i < colWidths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
        }

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBounds(15, 60, 680, 270);
        add(scrollPane);

        // ── Load data ──
        ArrayList<String[]> payments = customerService.getPaymentsByCustomer(user.getId());
        double total = 0.0;

        if (payments.isEmpty()) {
            JLabel noData = new JLabel("No payment records found.");
            noData.setForeground(Color.GRAY);
            noData.setBounds(265, 190, 220, 25);
            add(noData);
        } else {
            for (String[] p : payments) {
                // p: [PaymentID, AppointmentID, CustomerID, ServiceType, Amount, Date, Method]
                double amount = 0;
                try { amount = Double.parseDouble(p[4]); } catch (Exception ignored) {}
                total += amount;

                tableModel.addRow(new Object[]{
                    p[0],                          // Payment ID
                    p[1],                          // Appointment ID
                    p[3],                          // Service Type
                    String.format("%.2f", amount), // Amount formatted
                    p[5],                          // Date
                    p[6]                           // Payment method
                });
            }
        }

        // ── Total label ──
        JLabel lblTotal = new JLabel(String.format("Total Spent:  RM %.2f", total));
        lblTotal.setFont(new Font("Arial", Font.BOLD, 13));
        lblTotal.setForeground(new Color(0, 100, 0));
        lblTotal.setBounds(530, 345, 180, 28);
        add(lblTotal);

        JLabel lblCount = new JLabel("Total payments: " + payments.size());
        lblCount.setBounds(15, 350, 200, 25);
        lblCount.setForeground(Color.DARK_GRAY);
        add(lblCount);

        // ── Back action ──
        btnBack.addActionListener(e -> {
            dispose();
            new CustomerMenu(user);
        });

        setVisible(true);
    }
}
