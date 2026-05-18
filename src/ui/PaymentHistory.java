package ui;

import model.User;
import service.CustomerService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import java.awt.*;
import java.util.ArrayList;

/**
 * PaymentHistory shows all payment records for the logged-in customer,
 * including search filtering, sortable columns, and a running total.
 *
 * OOP Concepts:
 * - Encapsulation: CustomerService handles all file reading privately
 * - Inheritance: Extends JFrame
 */
public class PaymentHistory extends JFrame {

    private CustomerService customerService = new CustomerService();

    public PaymentHistory(User user) {

        setTitle("Payment History - " + user.getUsername());
        setSize(720, 510);
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

        // ── Search bar ──
        JLabel lblSearch = new JLabel("Search:");
        lblSearch.setBounds(15, 55, 55, 26);
        add(lblSearch);

        JTextField txtSearch = new JTextField();
        txtSearch.setBounds(75, 55, 200, 26);
        add(txtSearch);

        JLabel lblHint = new JLabel("(by service type, date, or payment method)");
        lblHint.setForeground(Color.GRAY);
        lblHint.setFont(new Font("Arial", Font.PLAIN, 11));
        lblHint.setBounds(285, 57, 300, 22);
        add(lblHint);

        // ── Record count ──
        JLabel lblCount = new JLabel("Total: 0");
        lblCount.setBounds(590, 55, 100, 26);
        lblCount.setForeground(Color.DARK_GRAY);
        add(lblCount);

        // ── Table ──
        String[] columns = {"Payment ID", "Appt ID", "Service Type", "Amount (RM)", "Date", "Method"};
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        JTable table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        table.setRowHeight(24);

        // Right-align Amount column
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        table.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);

        // Color Method column: Cash = orange tint, Card = blue tint
        table.getColumnModel().getColumn(5).setCellRenderer(
            (tbl, value, isSelected, hasFocus, row, col) -> {
                JLabel lbl = new JLabel(value == null ? "" : value.toString());
                lbl.setOpaque(true);
                lbl.setHorizontalAlignment(JLabel.CENTER);
                String method = value == null ? "" : value.toString();
                if (isSelected) {
                    lbl.setBackground(tbl.getSelectionBackground());
                    lbl.setForeground(tbl.getSelectionForeground());
                } else if (method.equalsIgnoreCase("Cash")) {
                    lbl.setBackground(new Color(255, 245, 220));
                    lbl.setForeground(new Color(150, 90, 0));
                } else if (method.equalsIgnoreCase("Card")) {
                    lbl.setBackground(new Color(220, 235, 255));
                    lbl.setForeground(new Color(0, 60, 160));
                } else {
                    lbl.setBackground(Color.WHITE);
                    lbl.setForeground(Color.BLACK);
                }
                return lbl;
            }
        );

        // Column widths
        int[] colWidths = {90, 80, 130, 110, 100, 90};
        for (int i = 0; i < colWidths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);

        // Sortable columns
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        // Default sort: Date descending (most recent payment first)
        sorter.setSortKeys(java.util.List.of(
            new RowSorter.SortKey(4, SortOrder.DESCENDING)
        ));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBounds(15, 90, 680, 320);
        add(scrollPane);

        // ── Load data ──
        ArrayList<String[]> payments = customerService.getPaymentsByCustomer(user.getId());
        double total = 0.0;

        if (payments.isEmpty()) {
            JLabel noData = new JLabel("No payment records found.");
            noData.setForeground(Color.GRAY);
            noData.setBounds(265, 240, 220, 25);
            add(noData);
        } else {
            for (String[] p : payments) {
                // p: [PaymentID, AppointmentID, CustomerID, ServiceType, Amount, Date, Method]
                double amount = 0;
                try { amount = Double.parseDouble(p[4]); } catch (Exception ignored) {}
                total += amount;

                tableModel.addRow(new Object[]{
                    p[0],                           // Payment ID
                    p[1],                           // Appointment ID
                    p[3],                           // Service Type
                    String.format("%.2f", amount),  // Amount
                    p[5],                           // Date
                    p[6]                            // Method
                });
            }
        }

        lblCount.setText("Total: " + payments.size());

        // ── Search filter ──
        // Searches: Service Type(2), Date(4), Method(5)
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            void filter() {
                String text = txtSearch.getText().trim();
                sorter.setRowFilter(text.isEmpty()
                    ? null
                    : RowFilter.regexFilter("(?i)" + text, 2, 4, 5));
            }
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
        });

        // ── Total spent label ──
        JLabel lblTotal = new JLabel(String.format("Total Spent:  RM %.2f", total));
        lblTotal.setFont(new Font("Arial", Font.BOLD, 13));
        lblTotal.setForeground(new Color(0, 100, 0));
        lblTotal.setBounds(510, 422, 200, 28);
        add(lblTotal);

        // ── Back action ──
        btnBack.addActionListener(e -> {
            dispose();
            new CustomerMenu(user);
        });

        setVisible(true);
    }
}