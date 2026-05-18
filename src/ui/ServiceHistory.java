package ui;

import model.User;
import service.CustomerService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

/**
 * ServiceHistory displays all appointments for the logged-in customer.
 *
 * OOP Concepts:
 * - Encapsulation: CustomerService hides file I/O; this class only calls public methods
 * - Inheritance: Uses User object passed from CustomerMenu (polymorphic user object)
 */
public class ServiceHistory extends JFrame {

    private CustomerService customerService = new CustomerService();

    public ServiceHistory(User user) {

        setTitle("Service History - " + user.getUsername());
        setSize(820, 420);
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

        // ── Table setup ──
        String[] columns = {"Appt ID", "Service Type", "Technician", "Date", "Time", "Duration", "Car Plate", "Vehicle", "Status"};
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false; // read-only table
            }
        };

        JTable table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);

        // Set column widths
        int[] colWidths = {70, 120, 90, 90, 60, 70, 80, 90, 90};
        for (int i = 0; i < colWidths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
        }

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBounds(15, 60, 780, 280);
        add(scrollPane);

        // ── Load data ──
        ArrayList<String[]> appointments = customerService.getAppointmentsByCustomer(user.getId());

        if (appointments.isEmpty()) {
            // Show a friendly message if no appointments exist
            JLabel noData = new JLabel("You have no service history yet.");
            noData.setForeground(Color.GRAY);
            noData.setBounds(300, 170, 250, 25);
            add(noData);
        } else {
            for (String[] appt : appointments) {
                // appt: [ID, CustomerID, ServiceType, TechnicianID, Date, Time, Duration, Plate, Model, Status]
                String techName = customerService.getUsernameById(appt[3]);
                String durationStr = appt[6] + (appt[6].equals("1") ? " hour" : " hours");

                tableModel.addRow(new Object[]{
                    appt[0],          // Appt ID
                    appt[2],          // Service Type
                    techName,         // Technician name (resolved from ID)
                    appt[4],          // Date
                    appt[5],          // Time
                    durationStr,      // Duration
                    appt[7],          // Car Plate
                    appt[8],          // Vehicle Model
                    appt[9]           // Status
                });
            }
        }

        // ── Summary label ──
        JLabel summary = new JLabel("Total appointments: " + appointments.size());
        summary.setBounds(15, 350, 200, 25);
        summary.setForeground(Color.DARK_GRAY);
        add(summary);

        // ── Back action ──
        btnBack.addActionListener(e -> {
            dispose();
            new CustomerMenu(user);
        });

        setVisible(true);
    }
}
