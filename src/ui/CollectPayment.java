package ui;

import model.User;
import service.PaymentService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * CollectPayment allows Counter Staff to:
 *  1. View all unpaid appointments
 *  2. Select one and collect payment (Cash or Card)
 *  3. Launch the receipt window after successful payment
 *
 * OOP Concepts:
 * - Encapsulation: PaymentService hides all file I/O behind public methods
 * - Abstraction: Counter staff just clicks "Collect Payment"; doesn't know how
 *                prices are loaded or how data is saved
 * - Inheritance: Extends JFrame
 * - Polymorphism: User object passed in is the same class used across all roles
 */
public class CollectPayment extends JFrame {

    private PaymentService paymentService = new PaymentService();
    private ArrayList<String[]> appointments;
    private DefaultTableModel tableModel;

    public CollectPayment(User user) {

        setTitle("Collect Payment");
        setSize(900, 560);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        setLocationRelativeTo(null);

        // ── Back button ──
        JButton btnBack = new JButton("< Back");
        btnBack.setBounds(15, 15, 80, 28);
        add(btnBack);

        // ── Title ──
        JLabel title = new JLabel("Collect Payment & Generate Receipt");
        title.setFont(new Font("Arial", Font.BOLD, 15));
        title.setBounds(240, 15, 380, 28);
        add(title);

        // ── Price info panel ──
        HashMap<String, Double> prices = paymentService.loadPrices();
        double normalPrice = prices.getOrDefault("Normal Service", 50.00);
        double majorPrice  = prices.getOrDefault("Major Service", 150.00);

        JPanel pricePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        pricePanel.setBounds(15, 50, 860, 35);
        pricePanel.setBackground(new Color(240, 248, 255));
        pricePanel.setBorder(BorderFactory.createLineBorder(new Color(150, 200, 255)));

        JLabel priceInfo = new JLabel(
            "Current Prices:   Normal Service = RM " + String.format("%.2f", normalPrice) +
            "     Major Service = RM " + String.format("%.2f", majorPrice)
        );
        priceInfo.setFont(new Font("Arial", Font.PLAIN, 12));
        pricePanel.add(priceInfo);
        add(pricePanel);

        // ── Search bar ──
        JLabel lblSearch = new JLabel("Search:");
        lblSearch.setBounds(15, 95, 55, 26);
        add(lblSearch);

        JTextField txtSearch = new JTextField();
        txtSearch.setBounds(75, 95, 220, 26);
        add(txtSearch);

        JLabel lblSearchHint = new JLabel("(by customer, service type, car plate, or date)");
        lblSearchHint.setForeground(Color.GRAY);
        lblSearchHint.setFont(new Font("Arial", Font.PLAIN, 11));
        lblSearchHint.setBounds(305, 97, 360, 22);
        add(lblSearchHint);

        // ── Appointment table ──
        String[] columns = {
            "Appt ID", "Customer", "Service Type", "Technician",
            "Date", "Time", "Car Plate", "Vehicle", "Status", "Amount (RM)"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        JTable table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        table.setRowHeight(24);

        // Right-align Amount column
        DefaultTableCellRenderer rightAlign = new DefaultTableCellRenderer();
        rightAlign.setHorizontalAlignment(JLabel.RIGHT);
        table.getColumnModel().getColumn(9).setCellRenderer(rightAlign);

        // Column widths
        int[] colWidths = {65, 90, 110, 90, 90, 55, 80, 80, 85, 90};
        for (int i = 0; i < colWidths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);

        // Sortable columns — click header to sort asc/desc
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        // Default: sort by Date (col 4) ascending
        sorter.setSortKeys(java.util.List.of(
            new RowSorter.SortKey(4, SortOrder.ASCENDING)
        ));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBounds(15, 135, 860, 240);
        add(scrollPane);

        // ── Bottom panel: payment method + collect button ──
        JLabel lblMethod = new JLabel("Payment Method:");
        lblMethod.setBounds(15, 380, 130, 28);
        add(lblMethod);

        JRadioButton rbCash = new JRadioButton("Cash", true);
        JRadioButton rbCard = new JRadioButton("Card / Online");
        ButtonGroup methodGroup = new ButtonGroup();
        methodGroup.add(rbCash);
        methodGroup.add(rbCard);
        rbCash.setBounds(155, 380, 70, 28);
        rbCard.setBounds(230, 380, 120, 28);
        rbCash.setBackground(getBackground());
        rbCard.setBackground(getBackground());
        add(rbCash);
        add(rbCard);

        JLabel lblSelected = new JLabel("No appointment selected.");
        lblSelected.setForeground(Color.GRAY);
        lblSelected.setBounds(15, 415, 500, 22);
        add(lblSelected);

        JButton btnCollect = new JButton("Collect Payment & Print Receipt");
        btnCollect.setBounds(580, 375, 280, 38);
        btnCollect.setFont(new Font("Arial", Font.BOLD, 13));
        btnCollect.setBackground(new Color(51, 153, 51));
        btnCollect.setForeground(Color.WHITE);
        btnCollect.setEnabled(false);
        add(btnCollect);

        // ── Load appointments ──
        loadAppointmentsIntoTable(prices);

        // ── Search filter ──
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            void filter() {
                String text = txtSearch.getText().trim();
                if (text.isEmpty()) {
                    sorter.setRowFilter(null);
                } else {
                    // Search across: Appt ID(0), Customer(1), ServiceType(2), Date(4), Plate(6)
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, 0, 1, 2, 4, 6));
                }
            }
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
        });

        // ── Row selection → update label & enable button ──
        table.getSelectionModel().addListSelectionListener(e -> {
            int viewRow = table.getSelectedRow();
            if (viewRow >= 0) {
                int modelRow = table.convertRowIndexToModel(viewRow);
                String apptID   = tableModel.getValueAt(modelRow, 0).toString();
                String customer = tableModel.getValueAt(modelRow, 1).toString();
                String service  = tableModel.getValueAt(modelRow, 2).toString();
                String amount   = tableModel.getValueAt(modelRow, 9).toString();
                lblSelected.setText("Selected: " + apptID + " | " + customer +
                                    " | " + service + " | RM " + amount);
                lblSelected.setForeground(new Color(0, 80, 0));
                btnCollect.setEnabled(true);
            }
        });

        // ── Collect payment ──
        btnCollect.addActionListener(e -> {
            int viewRow = table.getSelectedRow();
            if (viewRow < 0) {
                JOptionPane.showMessageDialog(this, "Please select an appointment.");
                return;
            }
            int row = table.convertRowIndexToModel(viewRow);

            String[] appt    = appointments.get(row);
            // appt: [ID, CustomerID, ServiceType, TechnicianID, Date, Time, Duration, Plate, Model, Status]
            String apptID    = appt[0];
            String custID    = appt[1];
            String service   = appt[2];
            String techID    = appt[3];
            String date      = appt[4];
            String time      = appt[5];
            String plate     = appt[7];
            String vehicle   = appt[8];

            String method    = rbCard.isSelected() ? "Card" : "Cash";
            double amount    = prices.getOrDefault(service, 0.00);

            String custName  = paymentService.getUsernameById(custID);
            String techName  = paymentService.getUsernameById(techID);

            // Confirm dialog
            int confirm = JOptionPane.showConfirmDialog(this,
                "Confirm payment of RM " + String.format("%.2f", amount) +
                " for " + custName + " via " + method + "?",
                "Confirm Payment",
                JOptionPane.YES_NO_OPTION
            );
            if (confirm != JOptionPane.YES_OPTION) return;

            // Save payment
            String paymentID = paymentService.savePayment(apptID, custID, service, amount, method);

            if (paymentID != null) {
                JOptionPane.showMessageDialog(this, "Payment collected successfully!");

                // Remove paid row from table
                tableModel.removeRow(row);
                appointments.remove(row);
                lblSelected.setText("No appointment selected.");
                lblSelected.setForeground(Color.GRAY);
                btnCollect.setEnabled(false);

                // Show receipt window
                new ReceiptWindow(
                    paymentID,
                    apptID,
                    custName,
                    techName,
                    service,
                    plate,
                    vehicle,
                    date,
                    time,
                    method,
                    amount
                );
            } else {
                JOptionPane.showMessageDialog(this,
                    "Failed to save payment. Please try again.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // ── Back ──
        btnBack.addActionListener(e -> {
            dispose();
            new CounterStaffMenu(user);
        });

        setVisible(true);
    }

    /**
     * Loads all unpaid appointments into the table, with calculated price.
     * Encapsulation: loading logic is separated — UI just displays what service returns.
     */
    private void loadAppointmentsIntoTable(HashMap<String, Double> prices) {
        appointments = paymentService.loadAllUnpaidAppointments();
        tableModel.setRowCount(0);

        if (appointments.isEmpty()) {
            // Show a subtle message — no data
            JLabel noData = new JLabel("No unpaid appointments found.");
            noData.setForeground(Color.GRAY);
            noData.setBounds(350, 190, 250, 25);
            add(noData);
            return;
        }

        for (String[] appt : appointments) {
            // appt: [ID, CustomerID, ServiceType, TechnicianID, Date, Time, Duration, Plate, Model, Status]
            String custName = paymentService.getUsernameById(appt[1]);
            String techName = paymentService.getUsernameById(appt[3]);
            double amount   = prices.getOrDefault(appt[2], 0.00);

            tableModel.addRow(new Object[]{
                appt[0],                          // Appt ID
                custName,                         // Customer name (resolved)
                appt[2],                          // Service Type
                techName,                         // Technician name (resolved)
                appt[4],                          // Date
                appt[5],                          // Time
                appt[7],                          // Car Plate
                appt[8],                          // Vehicle Model
                appt[9],                          // Status
                String.format("%.2f", amount)     // Amount
            });
        }
    }
}