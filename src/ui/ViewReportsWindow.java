package ui;

import model.User;
import service.ReportService;
import service.PaymentService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// GUI & Events: Swing components for reports viewing (OODJ Principle)
// Abstraction: Different report types filtered through abstraction methods
public class ViewReportsWindow {

    // Encapsulation: private file paths and service
    private final String APPOINTMENTS_FILE = "data/Appointments.txt";
    private final String USERS_FILE = "data/users.txt";
    private final ReportService reportService = new ReportService();
    private final PaymentService paymentService = new PaymentService();

    // Dashboard stat labels — kept as fields so we can refresh them later
    private JLabel lblRevenueValue;
    private JLabel lblTotalApptsValue;
    private JLabel lblCompletionValue;
    private JLabel lblTopTechValue;
    private JLabel lblLastUpdated;

    // Track current report type for export functionality
    private String currentReportType = "All Appointments";
    private DefaultTableModel currentModel;

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

        JLabel dashTitle = new JLabel("BUSINESS SUMMARY");
        dashTitle.setBounds(15, 8, 300, 20);
        dashTitle.setFont(dashTitle.getFont().deriveFont(Font.BOLD, 13f));
        dashTitle.setForeground(new Color(40, 80, 140));
        dashboardPanel.add(dashTitle);

        // Stat card 1: Total Revenue
        addStatCard(dashboardPanel, "Total Revenue", 20, 35);
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

        // Last updated timestamp at the bottom right of dashboard
        lblLastUpdated = new JLabel("Last updated: --");
        lblLastUpdated.setBounds(620, 108, 250, 16);
        lblLastUpdated.setFont(lblLastUpdated.getFont().deriveFont(Font.ITALIC, 10f));
        lblLastUpdated.setForeground(new Color(120, 120, 120));
        dashboardPanel.add(lblLastUpdated);

        // ===== REPORT CONTROLS =====
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

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.setBounds(560, 190, 110, 25);
        btnRefresh.setBackground(new Color(102, 204, 255));
        frame.add(btnRefresh);

        JButton btnExport = new JButton("Export CSV");
        btnExport.setBounds(680, 190, 120, 25);
        btnExport.setBackground(new Color(255, 204, 102));
        frame.add(btnExport);

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

        // Store table model reference for export
        currentModel = tableModel;

        // ActionListener: Generate report based on selected type
        btnGenerate.addActionListener(e -> {
            String selectedType = cmbReportType.getSelectedItem().toString();
            currentReportType = selectedType; // track current report for export

            // Always refresh the dashboard so latest data is shown
            refreshDashboard();

            // Abstraction: Different filter methods for different report types
            if (selectedType.equals("All Appointments")) {
                displayAllAppointments(tableModel, allAppointments);
            } else if (selectedType.equals("Service History (Completed)")) {
                displayServiceHistory(tableModel, allAppointments);
            } else if (selectedType.equals("Payment History")) {
                displayPaymentHistory(tableModel, allAppointments);
            } else if (selectedType.equals("Revenue Report")) {
                displayRevenueReport(tableModel);
            } else if (selectedType.equals("Technician Performance")) {
                displayTechnicianPerformance(tableModel);
            }
        });

        // ActionListener: Refresh — re-read all data from files
        btnRefresh.addActionListener(e -> {
            // Reload appointments list
            ArrayList<String[]> freshData = loadAppointments();
            allAppointments.clear();
            allAppointments.addAll(freshData);

            // Refresh dashboard and current report view
            refreshDashboard();

            // Re-render whatever report is currently selected
            String selectedType = cmbReportType.getSelectedItem().toString();
            if (selectedType.equals("All Appointments")) {
                displayAllAppointments(tableModel, allAppointments);
            } else if (selectedType.equals("Service History (Completed)")) {
                displayServiceHistory(tableModel, allAppointments);
            } else if (selectedType.equals("Payment History")) {
                displayPaymentHistory(tableModel, allAppointments);
            } else if (selectedType.equals("Revenue Report")) {
                displayRevenueReport(tableModel);
            } else if (selectedType.equals("Technician Performance")) {
                displayTechnicianPerformance(tableModel);
            }

            JOptionPane.showMessageDialog(frame, "Data refreshed from files.");
        });

        // ActionListener: Export current report to CSV file
        btnExport.addActionListener(e -> exportToCSV(frame));

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
        	String paymentStatus = paymentService.getPaymentStatus(appt[0]);

            model.addRow(new Object[]{
                appt[0],
                resolveUserName(appt[1]),      // Customer (was ID)
                appt[2], appt[4], appt[6], appt[9], paymentStatus
            });
        }
    }

    // ===== NEW ANALYTICS REPORT: Revenue per Service =====
    // Demonstrates business intelligence via aggregation
    private void displayRevenueReport(DefaultTableModel model) {
        model.setRowCount(0);
        model.setColumnIdentifiers(new String[]{
            "Service Type", "Completed Count", "Unit Price (RM)", "Revenue (RM)"
        });

        HashMap<String, Double> revenuePerService = reportService.calculateRevenuePerService();
        HashMap<String, Double> prices = reportService.loadPrices();

        // Use prices map keys to ensure all service types appear
        // even if revenue is 0 (no completed appointments yet)
        if (prices.isEmpty()) {
            model.addRow(new Object[]{"No service prices set", "", "", ""});
            return;
        }

        double grandTotal = 0.0;
        int totalCompleted = 0;

        for (Map.Entry<String, Double> entry : prices.entrySet()) {
            String service = entry.getKey();
            double unitPrice = entry.getValue();
            double revenue = revenuePerService.getOrDefault(service, 0.0);
            int count = (int) (unitPrice == 0 ? 0 : revenue / unitPrice);

            grandTotal += revenue;
            totalCompleted += count;

            model.addRow(new Object[]{
                service,
                count,
                String.format("%.2f", unitPrice),
                String.format("%.2f", revenue)
            });
        }

        // Add a separator row and grand total
        model.addRow(new Object[]{"", "", "", ""});
        model.addRow(new Object[]{
            "GRAND TOTAL",
            totalCompleted,
            "",
            String.format("RM %.2f", grandTotal)
        });
    }

    // ===== NEW ANALYTICS REPORT: Technician Performance =====
    // Demonstrates joining data from multiple files (appointments + feedback)
    private void displayTechnicianPerformance(DefaultTableModel model) {
        model.setRowCount(0);
        model.setColumnIdentifiers(new String[]{
            "Technician", "Total Appts", "Completed", "Completion %", "Avg Rating"
        });

        HashMap<String, Integer> totalPerTech = reportService.getAppointmentsPerTechnician();
        HashMap<String, Integer> completedPerTech = reportService.getCompletedPerTechnician();
        HashMap<String, Double> ratingsPerTech = reportService.getAverageRatingPerTechnician();

        if (totalPerTech.isEmpty()) {
            model.addRow(new Object[]{
                "No technician data available", "", "", "", ""
            });
            return;
        }

        for (Map.Entry<String, Integer> entry : totalPerTech.entrySet()) {
            String techId = entry.getKey();
            int total = entry.getValue();
            int completed = completedPerTech.getOrDefault(techId, 0);
            double completionPct = (total == 0) ? 0.0 : (completed * 100.0 / total);
            Double avgRating = ratingsPerTech.get(techId);

            model.addRow(new Object[]{
                reportService.resolveUserName(techId),
                total,
                completed,
                String.format("%.1f%%", completionPct),
                (avgRating == null) ? "No ratings" : String.format("%.2f / 5.0", avgRating)
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

        // Update last-updated timestamp
        String timestamp = LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm:ss"));
        lblLastUpdated.setText("Last updated: " + timestamp);
    }

    // ===== EXPORT TO CSV =====
    // Demonstrates File I/O (Lecture 9.1) — writes current report to a CSV file
    private void exportToCSV(JFrame parent) {
        if (currentModel == null || currentModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(parent,
                "No data to export. Please generate a report first.");
            return;
        }

        // Use JFileChooser for the Save As dialog (GUI principle)
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export Report to CSV");
        // Default filename based on report type and timestamp
        String defaultName = currentReportType.replaceAll("[^a-zA-Z0-9]", "_")
            + "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
            + ".csv";
        chooser.setSelectedFile(new File(defaultName));

        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) {
            return; // user cancelled
        }

        File outFile = chooser.getSelectedFile();
        // Ensure .csv extension
        if (!outFile.getName().toLowerCase().endsWith(".csv")) {
            outFile = new File(outFile.getAbsolutePath() + ".csv");
        }

        // File I/O: BufferedWriter to save CSV (Lecture 9.1)
        // Exception Handling: try-with-resources for safe file handling
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(outFile))) {

            // Write a header line with report title and date
            bw.write("APU Automotive Service Centre - " + currentReportType);
            bw.newLine();
            bw.write("Generated: " + LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm:ss")));
            bw.newLine();
            bw.newLine();

            // Write column headers
            int colCount = currentModel.getColumnCount();
            StringBuilder header = new StringBuilder();
            for (int c = 0; c < colCount; c++) {
                if (c > 0) header.append(",");
                header.append(escapeCsv(currentModel.getColumnName(c)));
            }
            bw.write(header.toString());
            bw.newLine();

            // Write data rows
            int rowCount = currentModel.getRowCount();
            for (int r = 0; r < rowCount; r++) {
                StringBuilder row = new StringBuilder();
                for (int c = 0; c < colCount; c++) {
                    if (c > 0) row.append(",");
                    Object val = currentModel.getValueAt(r, c);
                    row.append(escapeCsv(val == null ? "" : val.toString()));
                }
                bw.write(row.toString());
                bw.newLine();
            }

            JOptionPane.showMessageDialog(parent,
                "Report exported successfully!\n\nLocation: " + outFile.getAbsolutePath(),
                "Export Complete", JOptionPane.INFORMATION_MESSAGE);

        } catch (IOException ex) {
            // Exception Handling: graceful failure (Lecture 9.0)
            JOptionPane.showMessageDialog(parent,
                "Failed to export report: " + ex.getMessage(),
                "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Helper: Escape commas and quotes for safe CSV output
    private String escapeCsv(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}