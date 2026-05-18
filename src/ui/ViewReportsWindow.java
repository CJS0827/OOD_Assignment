package ui;

import model.User;
import service.ReportService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

// GUI & Events: Swing components for reports viewing (OODJ Principle)
// Abstraction: Different report types filtered through abstraction methods
public class ViewReportsWindow {

    // Encapsulation: private file paths and service
    private final String APPOINTMENTS_FILE = "data/Appointments.txt";
    private final String USERS_FILE = "data/users.txt";
    private final ReportService reportService = new ReportService();

    // Dashboard stat labels — kept as fields so we can refresh them later
    private JLabel lblRevenueValue;
    private JLabel lblTotalApptsValue;
    private JLabel lblCompletionValue;
    private JLabel lblTopTechValue;

    public ViewReportsWindow(User manager) {

        JFrame frame = new JFrame("View Reports");
        frame.setSize(950, 650);
        frame.setLayout(null);
        frame.setLocationRelativeTo(null);

        // Back button
        JButton btnBack = new JButton("< Back");
        btnBack.setBounds(30, 10, 90, 25);
        frame.add(btnBack);

        JLabel title = new JLabel("Analytical Reports");
        title.setBounds(370, 10, 200, 25);
        title.setFont(title.getFont().deriveFont(14.0f));
        frame.add(title);

     // ===== DASHBOARD PANEL (Summary Statistics) =====
        JPanel dashboardPanel = new JPanel(null);
        dashboardPanel.setBounds(30, 45, 880, 130);
        dashboardPanel.setBorder(new LineBorder(new Color(180, 180, 180), 1));
        dashboardPanel.setBackground(new Color(245, 248, 252));
        frame.add(dashboardPanel);

        JLabel dashTitle = new JLabel("📊  BUSINESS SUMMARY");
        dashTitle.setBounds(15, 8, 300, 20);
        dashTitle.setFont(dashTitle.getFont().deriveFont(Font.BOLD, 13f));
        dashTitle.setForeground(new Color(40, 80, 140));
        dashboardPanel.add(dashTitle);

        // Stat card 1: Total Revenue
        addStatCard(dashboardPanel, "Total Revenue",  20, 35);
        lblRevenueValue = addStatValue(dashboardPanel, "RM 0.00", 20, 70, new Color(0, 130, 60));

        // Stat card 2: Total Appointments
        addStatCard(dashboardPanel, "Total Appointments", 240, 35);
        lblTotalApptsValue = addStatValue(dashboardPanel, "0", 240, 70, new Color(40, 80, 160));

        // Stat card 3: Completion Rate
        addStatCard(dashboardPanel, "Completion Rate", 460, 35);
        lblCompletionValue = addStatValue(dashboardPanel, "0%", 460, 70, new Color(180, 100, 0));

        // Stat card 4: Top Technician
        addStatCard(dashboardPanel, "Top Technician", 680, 35);
        lblTopTechValue = addStatValue(dashboardPanel, "N/A", 680, 70, new Color(120, 40, 140));

        // ===== REPORT CONTROLS (moved down) =====
        JLabel lblReportType = new JLabel("Report Type:");
        lblReportType.setBounds(30, 190, 100, 25);
        frame.add(lblReportType);

        String[] reportTypes = {
            "All Appointments",
            "Service History (Completed)",
            "Payment History",
            "Revenue Report",
            "Technician Performance"
        };
        JComboBox<String> cmbReportType = new JComboBox<>(reportTypes);
        cmbReportType.setBounds(130, 190, 250, 25);
        frame.add(cmbReportType);

        JButton btnGenerate = new JButton("Generate Report");
        btnGenerate.setBounds(400, 190, 150, 25);
        frame.add(btnGenerate);

        // JTable for report display (GUI principle)
        DefaultTableModel tableModel = new DefaultTableModel();
        JTable table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBounds(30, 230, 880, 360);
        frame.add(scrollPane);

     // Refresh the dashboard stats from ReportService
        refreshDashboard();

        // Load all appointments initially
        ArrayList<String[]> allAppointments = loadAppointments();
        displayAllAppointments(tableModel, allAppointments);

        // ActionListener: Generate report based on selected type
        btnGenerate.addActionListener(e -> {
            String selectedType = cmbReportType.getSelectedItem().toString();

            // Always refresh the dashboard so latest data is shown
            refreshDashboard();

            // Abstraction: Different filter methods for different report types
            if (selectedType.equals("All Appointments")) {
                displayAllAppointments(tableModel, allAppointments);
            } else if (selectedType.equals("Service History (Completed)")) {
                displayServiceHistory(tableModel, allAppointments);
            } else if (selectedType.equals("Payment History")) {
                displayPaymentHistory(tableModel, allAppointments);
            }
            // Revenue Report and Technician Performance handlers added in next sub-task
        });

        // ActionListener: Back to Manager Menu
        btnBack.addActionListener(e -> {
            frame.dispose();
            new ManagerMenu(manager);
        });

        frame.setVisible(true);
    }

    
 // Helper: Resolve user ID to username for readable display
    private String resolveUserName(String userId) {
        File file = new File(USERS_FILE);
        if (!file.exists()) return userId;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split("\\|");
                if (data.length >= 2 && data[0].equalsIgnoreCase(userId)) {
                    return data[1]; // username
                }
            }
        } catch (IOException e) {
            // fallback to ID
        }
        return userId;
    }
    
    // File I/O: BufferedReader with String.split() to parse appointments
    private ArrayList<String[]> loadAppointments() {
        ArrayList<String[]> appointments = new ArrayList<>();
        File file = new File(APPOINTMENTS_FILE);

        if (!file.exists()) {
            return appointments;
        }

        // Exception Handling: try-with-resources for BufferedReader
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                // String.split() to parse pipe-delimited data
                String[] data = line.split("\\|");
                if (data.length >= 10) {
                    appointments.add(data);
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading appointments: " + e.getMessage());
        }

        return appointments;
    }

    // Abstraction: Display all appointments report
    private void displayAllAppointments(DefaultTableModel model, ArrayList<String[]> appointments) {
        model.setRowCount(0);
        model.setColumnIdentifiers(new String[]{
            "ID", "Customer", "Service Type", "Technician", "Date", "Time",
            "Duration", "Plate", "Model", "Status"
        });

        for (String[] appt : appointments) {
            model.addRow(new Object[]{
                appt[0],                       // ID
                resolveUserName(appt[1]),      // Customer (was ID)
                appt[2],                       // Service Type
                resolveUserName(appt[3]),      // Technician (was ID)
                appt[4], appt[5], appt[6], appt[7], appt[8], appt[9]
            });
        }
    }

    // Abstraction: Filter and display only completed appointments (Service History)
    private void displayServiceHistory(DefaultTableModel model, ArrayList<String[]> appointments) {
        model.setRowCount(0);
        model.setColumnIdentifiers(new String[]{
            "ID", "Customer", "Service Type", "Technician", "Date", "Time",
            "Duration", "Vehicle", "Status"
        });

        for (String[] appt : appointments) {
            // Filter: Only show completed appointments
            if (appt[9].equalsIgnoreCase("Completed")) {
                model.addRow(new Object[]{
                    appt[0],
                    resolveUserName(appt[1]),      // Customer
                    appt[2],
                    resolveUserName(appt[3]),      // Technician
                    appt[4], appt[5], appt[6], appt[8], appt[9]
                });
            }
        }

        if (model.getRowCount() == 0) {
            model.addRow(new Object[]{
                "No completed appointments found", "", "", "", "", "", "", "", ""
            });
        }
    }

 // Abstraction: Display payment history per appointment
    private void displayPaymentHistory(DefaultTableModel model, ArrayList<String[]> appointments) {
        model.setRowCount(0);
        model.setColumnIdentifiers(new String[]{
            "Appointment ID", "Customer", "Service Type", "Date",
            "Duration (hrs)", "Status", "Payment Status"
        });

        for (String[] appt : appointments) {
            // Determine payment status based on appointment status
            String paymentStatus = appt[9].equalsIgnoreCase("Completed") ? "Paid" : "Pending";

            model.addRow(new Object[]{
                appt[0],
                resolveUserName(appt[1]),      // Customer (was ID)
                appt[2], appt[4], appt[6], appt[9], paymentStatus
            });
        }
    }
 // Helper: Add a small caption label for a stat card
    private void addStatCard(JPanel panel, String caption, int x, int y) {
        JLabel lbl = new JLabel(caption);
        lbl.setBounds(x, y, 200, 20);
        lbl.setFont(lbl.getFont().deriveFont(Font.PLAIN, 11f));
        lbl.setForeground(new Color(90, 90, 90));
        panel.add(lbl);
    }

    // Helper: Add a large bold value label for a stat card
    private JLabel addStatValue(JPanel panel, String value, int x, int y, Color color) {
        JLabel lbl = new JLabel(value);
        lbl.setBounds(x, y, 220, 35);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 22f));
        lbl.setForeground(color);
        panel.add(lbl);
        return lbl;
    }

    // Refresh the 4 dashboard stat values from ReportService
    private void refreshDashboard() {
        double revenue = reportService.calculateTotalRevenue();
        int totalAppts = reportService.getTotalAppointments();
        double completion = reportService.getCompletionRate();
        String topTech = reportService.getTopTechnician();

        lblRevenueValue.setText("RM " + String.format("%.2f", revenue));
        lblTotalApptsValue.setText(String.valueOf(totalAppts));
        lblCompletionValue.setText(String.format("%.1f%%", completion));
        lblTopTechValue.setText(topTech);
    }
}
