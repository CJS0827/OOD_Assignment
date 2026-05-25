package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import java.awt.*;
import java.util.ArrayList;
import model.User;
import service.TechnicianService;

/**
 * TechAppointmentsPanel shows all appointments assigned to the technician.
 * The technician can view details and mark a Scheduled appointment as Completed.
 *
 * OOP Concepts:
 * - Inheritance   : extends JFrame
 * - Encapsulation : TechnicianService handles all file I/O
 * - Abstraction   : technician uses a clean table without knowing file details
 * - Polymorphism  : isCellEditable() is overridden in the table model
 */
public class TechAppointmentsPanel extends JFrame {

    private TechnicianService techService = new TechnicianService();

    public TechAppointmentsPanel(User user) {

        setTitle("My Appointments - " + user.getUsername());
        setSize(820, 530);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        setLocationRelativeTo(null);

        // Title
        JLabel title = new JLabel("My Assigned Appointments");
        title.setFont(new Font("Arial", Font.BOLD, 16));
        title.setBounds(280, 15, 300, 30);
        add(title);

        // Back button
        JButton btnBack = new JButton("< Back");
        btnBack.setBounds(15, 15, 80, 28);
        add(btnBack);

        // Search bar
        JLabel lblSearch = new JLabel("Search:");
        lblSearch.setBounds(15, 55, 55, 26);
        add(lblSearch);

        JTextField txtSearch = new JTextField();
        txtSearch.setBounds(75, 55, 200, 26);
        add(txtSearch);

        JLabel lblHint = new JLabel("(by ID, service type, date, plate, or status)");
        lblHint.setForeground(Color.GRAY);
        lblHint.setFont(new Font("Arial", Font.PLAIN, 11));
        lblHint.setBounds(285, 57, 310, 22);
        add(lblHint);

        JLabel lblCount = new JLabel("Total: 0");
        lblCount.setBounds(690, 55, 100, 26);
        lblCount.setForeground(Color.DARK_GRAY);
        add(lblCount);

        // Table
        String[] columns = {"Appt ID", "Customer", "Service Type", "Date", "Time", "Hrs", "Plate", "Car Model", "Status"};
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        JTable table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        table.setRowHeight(24);

        int[] colWidths = {70, 90, 115, 90, 58, 40, 80, 100, 90};
        for (int i = 0; i < colWidths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);

        // Colour Status column: Completed = green, Scheduled = blue
        table.getColumnModel().getColumn(8).setCellRenderer(
            (tbl, value, isSelected, hasFocus, row, col) -> {
                JLabel lbl = new JLabel(value == null ? "" : value.toString());
                lbl.setOpaque(true);
                lbl.setHorizontalAlignment(JLabel.CENTER);
                String s = value == null ? "" : value.toString();
                if (isSelected) {
                    lbl.setBackground(tbl.getSelectionBackground());
                    lbl.setForeground(tbl.getSelectionForeground());
                } else if (s.equalsIgnoreCase("Completed")) {
                    lbl.setBackground(new Color(220, 255, 220));
                    lbl.setForeground(new Color(0, 120, 0));
                } else if (s.equalsIgnoreCase("Scheduled")) {
                    lbl.setBackground(new Color(220, 235, 255));
                    lbl.setForeground(new Color(0, 60, 160));
                } else {
                    lbl.setBackground(Color.WHITE);
                    lbl.setForeground(Color.BLACK);
                }
                return lbl;
            }
        );

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        sorter.setSortKeys(java.util.List.of(new RowSorter.SortKey(3, SortOrder.ASCENDING)));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBounds(15, 90, 780, 250);
        add(scrollPane);

        // Detail area
        JLabel lblDetail = new JLabel("Click a row to see full details:");
        lblDetail.setForeground(Color.DARK_GRAY);
        lblDetail.setBounds(15, 348, 250, 20);
        add(lblDetail);

        JTextArea detailArea = new JTextArea();
        detailArea.setEditable(false);
        detailArea.setLineWrap(true);
        detailArea.setWrapStyleWord(true);
        detailArea.setBackground(new Color(245, 245, 245));
        detailArea.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        JScrollPane detailScroll = new JScrollPane(detailArea);
        detailScroll.setBounds(15, 370, 780, 60);
        add(detailScroll);

        // Mark complete button
        JButton btnComplete = new JButton("Mark as Completed");
        btnComplete.setBounds(15, 443, 180, 30);
        btnComplete.setEnabled(false);
        add(btnComplete);

        JLabel btnHint = new JLabel("Select a 'Scheduled' appointment to mark it complete.");
        btnHint.setForeground(Color.GRAY);
        btnHint.setFont(new Font("Arial", Font.PLAIN, 11));
        btnHint.setBounds(205, 450, 370, 18);
        add(btnHint);

        // Load data
        ArrayList<String[]> appointments = techService.getAppointmentsByTechnician(user.getId());
        for (String[] a : appointments) {
            String customerName = techService.getUsernameById(a[1]);
            tableModel.addRow(new Object[]{
                a[0], customerName, a[2], a[4], a[5], a[6], a[7], a[8], a[9]
            });
        }
        lblCount.setText("Total: " + appointments.size());

        // Row click → show details, enable button if Scheduled
        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int vr = table.getSelectedRow();
            if (vr < 0) { detailArea.setText(""); btnComplete.setEnabled(false); return; }
            int mr = table.convertRowIndexToModel(vr);
            detailArea.setText(
                "Appointment: " + tableModel.getValueAt(mr,0) +
                "   Customer: " + tableModel.getValueAt(mr,1) +
                "   Service: "  + tableModel.getValueAt(mr,2) + "\n" +
                "Date: "        + tableModel.getValueAt(mr,3) +
                "   Time: "     + tableModel.getValueAt(mr,4) +
                "   Duration: " + tableModel.getValueAt(mr,5) + " hour(s)" +
                "   Plate: "    + tableModel.getValueAt(mr,6) +
                "   Car: "      + tableModel.getValueAt(mr,7) +
                "   Status: "   + tableModel.getValueAt(mr,8));
            String status = tableModel.getValueAt(mr,8).toString();
            btnComplete.setEnabled(status.equalsIgnoreCase("Scheduled"));
        });

        // Mark complete
        btnComplete.addActionListener(e -> {
            int vr = table.getSelectedRow();
            if (vr < 0) return;
            int mr = table.convertRowIndexToModel(vr);
            String apptID = tableModel.getValueAt(mr,0).toString();
            int confirm = JOptionPane.showConfirmDialog(this,
                "Mark appointment " + apptID + " as Completed?",
                "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;
            if (techService.markAppointmentCompleted(apptID)) {
                JOptionPane.showMessageDialog(this, apptID + " marked as Completed.");
                dispose();
                new TechAppointmentsPanel(user);
            } else {
                JOptionPane.showMessageDialog(this, "Update failed. Please try again.");
            }
        });

        // Search filter
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            void filter() {
                String t = txtSearch.getText().trim();
                sorter.setRowFilter(t.isEmpty() ? null : RowFilter.regexFilter("(?i)" + t, 0, 2, 3, 6, 8));
            }
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
        });

        btnBack.addActionListener(e -> {
            dispose();
            new TechnicianMenu(user);
        });

        setVisible(true);
    }
}
