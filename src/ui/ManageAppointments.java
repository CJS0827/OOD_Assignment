package ui;

import model.User;
import service.AppointmentService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import com.toedter.calendar.JDateChooser;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import javax.swing.DefaultComboBoxModel;

public class ManageAppointments {

    private AppointmentService service = new AppointmentService();
    private DefaultTableModel tableModel;
    private ArrayList<String[]> appointments;

    public ManageAppointments(User user) {

        JFrame frame = new JFrame("Manage Appointments");
        frame.setSize(950, 580);
        frame.setLayout(null);
        frame.setLocationRelativeTo(null);

        // ── Back ──
        JButton btnBack = new JButton("< Back");
        btnBack.setBounds(15, 15, 80, 28);
        frame.add(btnBack);

        JLabel title = new JLabel("Manage Appointments — Reschedule or Cancel");
        title.setFont(new Font("Arial", Font.BOLD, 14));
        title.setBounds(200, 15, 450, 28);
        frame.add(title);

        // ── Search ──
        JLabel lblSearch = new JLabel("Search:");
        lblSearch.setBounds(15, 55, 55, 25);
        frame.add(lblSearch);

        JTextField txtSearch = new JTextField();
        txtSearch.setBounds(75, 55, 200, 25);
        frame.add(txtSearch);

        JLabel lblHint = new JLabel("(by customer, plate, status, or date)");
        lblHint.setForeground(Color.GRAY);
        lblHint.setFont(new Font("Arial", Font.PLAIN, 11));
        lblHint.setBounds(285, 57, 280, 22);
        frame.add(lblHint);

        // ── Table ──
        String[] columns = {
            "Appt ID", "Customer", "Service Type", "Technician",
            "Date", "Time", "Duration", "Car Plate", "Vehicle", "Status"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        table.setRowHeight(24);

        int[] colWidths = {65, 85, 110, 90, 90, 55, 65, 80, 85, 85};
        for (int i = 0; i < colWidths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        sorter.setSortKeys(java.util.List.of(new RowSorter.SortKey(4, SortOrder.ASCENDING)));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBounds(15, 90, 910, 280);
        frame.add(scrollPane);

        // ── Action buttons ──
        JButton btnReschedule = new JButton("Reschedule");
        btnReschedule.setBounds(300, 385, 140, 32);
        btnReschedule.setBackground(new Color(51, 153, 255));
        btnReschedule.setForeground(Color.WHITE);
        btnReschedule.setEnabled(false);
        frame.add(btnReschedule);

        JButton btnCancel = new JButton("Cancel Appointment");
        btnCancel.setBounds(460, 385, 170, 32);
        btnCancel.setBackground(new Color(204, 51, 51));
        btnCancel.setForeground(Color.WHITE);
        btnCancel.setEnabled(false);
        frame.add(btnCancel);

        JLabel lblSelected = new JLabel("No appointment selected.");
        lblSelected.setForeground(Color.GRAY);
        lblSelected.setBounds(15, 430, 600, 22);
        frame.add(lblSelected);

        // ── Load data ──
        loadTable();

        // ── Search filter ──
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            void filter() {
                String text = txtSearch.getText().trim();
                sorter.setRowFilter(text.isEmpty() ? null :
                    RowFilter.regexFilter("(?i)" + text, 1, 2, 4, 7, 9));
            }
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
        });

        // ── Row selection ──
        table.getSelectionModel().addListSelectionListener(e -> {
            int viewRow = table.getSelectedRow();
            if (viewRow < 0) return;
            int modelRow = table.convertRowIndexToModel(viewRow);
            String apptID  = tableModel.getValueAt(modelRow, 0).toString();
            String status  = tableModel.getValueAt(modelRow, 9).toString();

            lblSelected.setText("Selected: " + apptID + " | Status: " + status);
            lblSelected.setForeground(Color.DARK_GRAY);

            // Only allow reschedule/cancel if Scheduled
            boolean isScheduled = status.equalsIgnoreCase("Scheduled");
            btnReschedule.setEnabled(isScheduled);
            btnCancel.setEnabled(isScheduled);

            if (!isScheduled) {
                lblSelected.setText("Selected: " + apptID +
                    " | Status: " + status + " — only Scheduled appointments can be modified.");
                lblSelected.setForeground(new Color(180, 80, 0));
            }
        });

        // ── Reschedule ──
        btnReschedule.addActionListener(e -> {
            int viewRow = table.getSelectedRow();
            if (viewRow < 0) return;
            int modelRow = table.convertRowIndexToModel(viewRow);
            String[] appt = appointments.get(modelRow);
            showRescheduleDialog(frame, appt, user);
        });

        // ── Cancel ──
        btnCancel.addActionListener(e -> {
            int viewRow = table.getSelectedRow();
            if (viewRow < 0) return;
            int modelRow = table.convertRowIndexToModel(viewRow);
            String[] appt = appointments.get(modelRow);

            int confirm = JOptionPane.showConfirmDialog(frame,
                "Are you sure you want to cancel appointment " + appt[0] + "?\nThis cannot be undone.",
                "Confirm Cancellation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );
            if (confirm != JOptionPane.YES_OPTION) return;

            if (service.updateAppointmentStatus(appt[0], "Cancelled")) {
                JOptionPane.showMessageDialog(frame, "Appointment " + appt[0] + " has been cancelled.");
                loadTable();
                lblSelected.setText("No appointment selected.");
                lblSelected.setForeground(Color.GRAY);
                btnReschedule.setEnabled(false);
                btnCancel.setEnabled(false);
            } else {
                JOptionPane.showMessageDialog(frame, "Failed to cancel. Please try again.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnBack.addActionListener(e -> {
            frame.dispose();
            new CounterStaffMenu(user);
        });

        frame.setVisible(true);
    }

    private void loadTable() {
        appointments = service.loadAllAppointments();
        tableModel.setRowCount(0);
        for (String[] appt : appointments) {
            String custName = service.getUsernameById(appt[1]);
            String techName = service.getUsernameById(appt[3]);
            tableModel.addRow(new Object[]{
                appt[0], custName, appt[2], techName,
                appt[4], appt[5], appt[6], appt[7], appt[8], appt[9]
            });
        }
    }

    private void showRescheduleDialog(JFrame parent, String[] appt, User user) {
        JDialog dialog = new JDialog(parent, "Reschedule Appointment " + appt[0], true);
        dialog.setSize(380, 280);
        dialog.setLayout(null);
        dialog.setLocationRelativeTo(parent);

        JLabel l1 = new JLabel("New Date:");
        l1.setBounds(20, 20, 100, 25);
        dialog.add(l1);

        JDateChooser dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("yyyy-MM-dd");
        dateChooser.setBounds(130, 20, 180, 25);
        dialog.add(dateChooser);

        JLabel l2 = new JLabel("New Time:");
        l2.setBounds(20, 60, 100, 25);
        dialog.add(l2);

        JSpinner hourSpinner   = new JSpinner(new SpinnerNumberModel(8, 0, 23, 1));
        JSpinner minuteSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 45, 15));
        hourSpinner.setEditor(new JSpinner.NumberEditor(hourSpinner, "00"));
        minuteSpinner.setEditor(new JSpinner.NumberEditor(minuteSpinner, "00"));
        hourSpinner.setBounds(130, 60, 70, 25);
        minuteSpinner.setBounds(210, 60, 70, 25);
        JLabel colon = new JLabel(":");
        colon.setBounds(202, 60, 10, 25);
        dialog.add(hourSpinner);
        dialog.add(colon);
        dialog.add(minuteSpinner);

        JLabel l3 = new JLabel("Technician:");
        l3.setBounds(20, 100, 100, 25);
        dialog.add(l3);

        // Load available technicians
        String[] techEntries = service.loadTechnicians();
        String[] techNames   = extractNames(techEntries);
        JComboBox<String> techBox = new JComboBox<>(techNames);
        techBox.setBounds(130, 100, 180, 25);
        dialog.add(techBox);

        JButton btnConfirm = new JButton("Confirm Reschedule");
        btnConfirm.setBounds(90, 160, 190, 30);
        btnConfirm.setBackground(new Color(51, 153, 255));
        btnConfirm.setForeground(Color.WHITE);
        dialog.add(btnConfirm);

        JButton btnDialogCancel = new JButton("Cancel");
        btnDialogCancel.setBounds(90, 200, 190, 25);
        dialog.add(btnDialogCancel);

        btnConfirm.addActionListener(e -> {
            String newDate = dateChooser.getDate() != null
                ? new SimpleDateFormat("yyyy-MM-dd").format(dateChooser.getDate()) : "";
            String newTime = String.format("%02d:%02d",
                (int) hourSpinner.getValue(), (int) minuteSpinner.getValue());
            int techIndex  = techBox.getSelectedIndex();
            String newTechID = extractId(techEntries, techIndex);

            if (newDate.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please select a new date.");
                return;
            }

            if (service.rescheduleAppointment(appt[0], newDate, newTime, newTechID)) {
                JOptionPane.showMessageDialog(dialog,
                    "Appointment " + appt[0] + " rescheduled successfully!");
                dialog.dispose();
                loadTable();
            } else {
                JOptionPane.showMessageDialog(dialog,
                    "Failed to reschedule. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnDialogCancel.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }

    private String[] extractNames(String[] entries) {
        String[] names = new String[entries.length];
        for (int i = 0; i < entries.length; i++) {
            String[] parts = entries[i].split("\\|");
            names[i] = parts.length >= 2 ? parts[1] : parts[0];
        }
        return names;
    }

    private String extractId(String[] entries, int index) {
        if (index < 0 || index >= entries.length) return "";
        return entries[index].split("\\|")[0];
    }
}