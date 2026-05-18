package ui;

import model.User;
import service.PaymentService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

/**
 * PaymentRecords allows Counter Staff to view all completed payment records
 * and re-download any receipt as a PDF.
 *
 * OOP Concepts:
 * - Encapsulation: PaymentService hides all file I/O behind public methods
 * - Abstraction: clicking "Download Receipt" triggers generatePDF() inside
 *                ReceiptWindow without this class knowing the PDF details
 * - Inheritance: extends JFrame
 * - Polymorphism: User object passed in same as all other menu screens
 */
public class PaymentRecords extends JFrame {

    private PaymentService paymentService = new PaymentService();
    private DefaultTableModel tableModel;
    private ArrayList<String[]> payments;       // raw payment records
    private ArrayList<String[]> appointments;   // raw appointment records (for PDF details)

    public PaymentRecords(User user) {

        setTitle("Payment Records");
        setSize(950, 520);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        setLocationRelativeTo(null);

        // ── Back button ──
        JButton btnBack = new JButton("< Back");
        btnBack.setBounds(15, 15, 80, 28);
        add(btnBack);

        // ── Title ──
        JLabel title = new JLabel("Completed Payment Records");
        title.setFont(new Font("Arial", Font.BOLD, 15));
        title.setBounds(310, 15, 340, 28);
        add(title);

        // ── Search bar ──
        JLabel lblSearch = new JLabel("Search:");
        lblSearch.setBounds(15, 55, 55, 26);
        add(lblSearch);

        JTextField txtSearch = new JTextField();
        txtSearch.setBounds(75, 55, 220, 26);
        add(txtSearch);

        JLabel lblSearchHint = new JLabel("(by customer, appt ID, service type, or date)");
        lblSearchHint.setForeground(Color.GRAY);
        lblSearchHint.setFont(new Font("Arial", Font.PLAIN, 11));
        lblSearchHint.setBounds(305, 57, 320, 22);
        add(lblSearchHint);

        // ── Summary labels (top right) ──
        JLabel lblTotalCount = new JLabel("Total records: 0");
        lblTotalCount.setBounds(700, 52, 140, 22);
        lblTotalCount.setForeground(Color.DARK_GRAY);
        add(lblTotalCount);

        JLabel lblTotalAmount = new JLabel("Total collected: RM 0.00");
        lblTotalAmount.setBounds(700, 72, 200, 22);
        lblTotalAmount.setFont(new Font("Arial", Font.BOLD, 12));
        lblTotalAmount.setForeground(new Color(0, 100, 0));
        add(lblTotalAmount);

        // ── Table ──
        String[] columns = {
            "Payment ID", "Appt ID", "Customer", "Service Type",
            "Technician", "Car Plate", "Date", "Method", "Amount (RM)"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        JTable table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        table.setRowHeight(25);

        // Right-align Amount column
        DefaultTableCellRenderer rightAlign = new DefaultTableCellRenderer();
        rightAlign.setHorizontalAlignment(JLabel.RIGHT);
        table.getColumnModel().getColumn(8).setCellRenderer(rightAlign);

        // Column widths
        int[] colWidths = {85, 75, 100, 120, 100, 80, 90, 70, 95};
        for (int i = 0; i < colWidths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);

        // Allow sorting by clicking column headers
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        
        sorter.setSortKeys(java.util.List.of(
        	    new RowSorter.SortKey(0, SortOrder.DESCENDING)
        	));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBounds(15, 95, 910, 320);
        add(scrollPane);

        // ── Bottom panel ──
        JLabel lblSelected = new JLabel("Select a payment record to download its receipt.");
        lblSelected.setForeground(Color.GRAY);
        lblSelected.setBounds(15, 425, 500, 24);
        add(lblSelected);

        JButton btnDownload = new JButton("⬇  Download Receipt (PDF)");
        btnDownload.setBounds(660, 420, 250, 36);
        btnDownload.setFont(new Font("Arial", Font.BOLD, 12));
        btnDownload.setBackground(new Color(180, 30, 30));
        btnDownload.setForeground(Color.WHITE);
        btnDownload.setFocusPainted(false);
        btnDownload.setEnabled(false);
        add(btnDownload);

        // ── Load data ──
        payments     = paymentService.loadAllPayments();
        appointments = paymentService.loadAllAppointmentsRaw();

        double totalAmount = 0;
        for (String[] p : payments) {
            // p: [PaymentID, AppointmentID, CustomerID, ServiceType, Amount, Date, Method]
            String custName = paymentService.getUsernameById(p[2]);
            String techName = getTechnicianForAppointment(p[1]);
            String plate    = getPlateForAppointment(p[1]);
            double amount   = 0;
            try { amount = Double.parseDouble(p[4]); } catch (Exception ignored) {}
            totalAmount += amount;

            tableModel.addRow(new Object[]{
                p[0],                           // Payment ID
                p[1],                           // Appt ID
                custName,                       // Customer name
                p[3],                           // Service Type
                techName,                       // Technician name
                plate,                          // Car Plate
                p[5],                           // Date
                p[6],                           // Method
                String.format("%.2f", amount)   // Amount
            });
        }

        // Update summary
        lblTotalCount.setText("Total records: " + payments.size());
        lblTotalAmount.setText(String.format("Total collected: RM %.2f", totalAmount));

        // ── Search filter ──
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            void filter() {
                String text = txtSearch.getText().trim();
                if (text.isEmpty()) {
                    sorter.setRowFilter(null);
                } else {
                    // Search across Customer (col2), ApptID (col1), ServiceType (col3), Date (col6)
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, 1, 2, 3, 6));
                }
            }
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
        });

        // ── Row selection → enable download ──
        table.getSelectionModel().addListSelectionListener(e -> {
            int viewRow = table.getSelectedRow();
            if (viewRow < 0) {
                btnDownload.setEnabled(false);
                lblSelected.setText("Select a payment record to download its receipt.");
                lblSelected.setForeground(Color.GRAY);
                return;
            }
            int modelRow = table.convertRowIndexToModel(viewRow);
            String pid      = tableModel.getValueAt(modelRow, 0).toString();
            String customer = tableModel.getValueAt(modelRow, 2).toString();
            String service  = tableModel.getValueAt(modelRow, 3).toString();
            String amount   = tableModel.getValueAt(modelRow, 8).toString();

            lblSelected.setText("Selected: " + pid + " | " + customer + " | " + service + " | RM " + amount);
            lblSelected.setForeground(new Color(0, 80, 0));
            btnDownload.setEnabled(true);
        });

        // ── Download receipt ──
        btnDownload.addActionListener(e -> {
            int viewRow = table.getSelectedRow();
            if (viewRow < 0) return;
            int modelRow = table.convertRowIndexToModel(viewRow);

            // Retrieve all payment data from the model row
            String paymentID  = tableModel.getValueAt(modelRow, 0).toString();
            String apptID     = tableModel.getValueAt(modelRow, 1).toString();
            String custName   = tableModel.getValueAt(modelRow, 2).toString();
            String service    = tableModel.getValueAt(modelRow, 3).toString();
            String techName   = tableModel.getValueAt(modelRow, 4).toString();
            String plate      = tableModel.getValueAt(modelRow, 5).toString();
            String date       = tableModel.getValueAt(modelRow, 6).toString();
            String method     = tableModel.getValueAt(modelRow, 7).toString();
            double amount     = 0;
            try { amount = Double.parseDouble(tableModel.getValueAt(modelRow, 8).toString()); }
            catch (Exception ignored) {}

            // Look up extra appointment details (time, vehicle model)
            String time    = getTimeForAppointment(apptID);
            String vehicle = getVehicleForAppointment(apptID);

            // Open ReceiptWindow — it handles the PDF download internally (Abstraction)
            new ReceiptWindow(
                paymentID, apptID, custName, techName,
                service, plate, vehicle, date, time, method, amount
            );
        });

        // ── Back ──
        btnBack.addActionListener(e -> {
            dispose();
            new CounterStaffMenu(user);
        });

        setVisible(true);
    }

    // ── Private helpers to look up appointment details ──
    // Encapsulation: these details are resolved internally, not exposed to other classes

    private String getTechnicianForAppointment(String apptID) {
        for (String[] a : appointments) {
            if (a.length >= 4 && a[0].equalsIgnoreCase(apptID))
                return paymentService.getUsernameById(a[3]);
        }
        return "N/A";
    }

    private String getPlateForAppointment(String apptID) {
        for (String[] a : appointments) {
            if (a.length >= 8 && a[0].equalsIgnoreCase(apptID))
                return a[7];
        }
        return "N/A";
    }

    private String getTimeForAppointment(String apptID) {
        for (String[] a : appointments) {
            if (a.length >= 6 && a[0].equalsIgnoreCase(apptID))
                return a[5];
        }
        return "N/A";
    }

    private String getVehicleForAppointment(String apptID) {
        for (String[] a : appointments) {
            if (a.length >= 9 && a[0].equalsIgnoreCase(apptID))
                return a[8];
        }
        return "N/A";
    }
}
