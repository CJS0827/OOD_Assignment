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

/**
 * ServiceHistory displays all appointments for the logged-in customer.
 * Includes search filtering and sortable columns.
 *
 * OOP Concepts:
 * - Encapsulation: CustomerService hides file I/O; this class only calls public methods
 * - Inheritance: extends JFrame; Uses User object passed from CustomerMenu
 */
public class ServiceHistory extends JFrame {

    private CustomerService customerService = new CustomerService();

    public ServiceHistory(User user) {

        setTitle("Service History - " + user.getUsername());
        setSize(820, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        setLocationRelativeTo(null);

        // ── Title ──
        JLabel title = new JLabel("My Service History");
        title.setFont(new Font("Arial", Font.BOLD, 16));
        title.setBounds(300, 15, 250, 30);
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

        JLabel lblHint = new JLabel("(by service type, technician, date, plate, or status)");
        lblHint.setForeground(Color.GRAY);
        lblHint.setFont(new Font("Arial", Font.PLAIN, 11));
        lblHint.setBounds(285, 57, 340, 22);
        add(lblHint);

        // ── Record count ──
        JLabel lblCount = new JLabel("Total: 0");
        lblCount.setBounds(680, 55, 100, 26);
        lblCount.setForeground(Color.DARK_GRAY);
        add(lblCount);

        // ── Table ──
        String[] columns = {"Appt ID", "Service Type", "Technician", "Date", "Time", "Duration", "Car Plate", "Vehicle", "Status"};
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        JTable table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        table.setRowHeight(24);

        // Column widths
        int[] colWidths = {70, 120, 100, 90, 60, 75, 80, 90, 85};
        for (int i = 0; i < colWidths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);

        // Color Status column: Scheduled = blue, Completed = green
        table.getColumnModel().getColumn(8).setCellRenderer(
            (tbl, value, isSelected, hasFocus, row, col) -> {
                JLabel lbl = new JLabel(value == null ? "" : value.toString());
                lbl.setOpaque(true);
                lbl.setHorizontalAlignment(JLabel.CENTER);
                String status = value == null ? "" : value.toString();
                if (isSelected) {
                    lbl.setBackground(tbl.getSelectionBackground());
                    lbl.setForeground(tbl.getSelectionForeground());
                } else if (status.equalsIgnoreCase("Completed")) {
                    lbl.setBackground(new Color(220, 255, 220));
                    lbl.setForeground(new Color(0, 120, 0));
                } else if (status.equalsIgnoreCase("Scheduled")) {
                    lbl.setBackground(new Color(220, 235, 255));
                    lbl.setForeground(new Color(0, 60, 160));
                } else {
                    lbl.setBackground(Color.WHITE);
                    lbl.setForeground(Color.BLACK);
                }
                return lbl;
            }
        );

        // Sortable columns — click header to sort asc/desc
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        // Default sort: Date descending (most recent first)
        sorter.setSortKeys(java.util.List.of(
            new RowSorter.SortKey(3, SortOrder.DESCENDING)
        ));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBounds(15, 90, 780, 330);
        add(scrollPane);

        // ── Load data ──
        ArrayList<String[]> appointments = customerService.getAppointmentsByCustomer(user.getId());

        if (appointments.isEmpty()) {
            JLabel noData = new JLabel("You have no service history yet.");
            noData.setForeground(Color.GRAY);
            noData.setBounds(300, 250, 250, 25);
            add(noData);
        } else {
            for (String[] appt : appointments) {
                // appt: [ID, CustomerID, ServiceType, TechnicianID, Date, Time, Duration, Plate, Model, Status]
                String techName    = customerService.getUsernameById(appt[3]);
                String durationStr = appt[6] + (appt[6].equals("1") ? " hour" : " hours");

                tableModel.addRow(new Object[]{
                    appt[0],       // Appt ID
                    appt[2],       // Service Type
                    techName,      // Technician name
                    appt[4],       // Date
                    appt[5],       // Time
                    durationStr,   // Duration
                    appt[7],       // Car Plate
                    appt[8],       // Vehicle Model
                    appt[9]        // Status
                });
            }
        }

        lblCount.setText("Total: " + appointments.size());

        // ── Search filter ──
        // Searches: Service Type(1), Technician(2), Date(3), Car Plate(6), Status(8)
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            void filter() {
                String text = txtSearch.getText().trim();
                sorter.setRowFilter(text.isEmpty()
                    ? null
                    : RowFilter.regexFilter("(?i)" + text, 1, 2, 3, 6, 8));
            }
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
        });

        // ── Back action ──
        btnBack.addActionListener(e -> {
            dispose();
            new CustomerMenu(user);
        });

        setVisible(true);
    }
}