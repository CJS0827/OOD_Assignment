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
 * ViewComments shows all comments the customer has previously submitted,
 * with search filtering and sortable columns.
 *
 * OOP Concepts:
 * - Encapsulation: CustomerService hides file I/O; this class only calls public methods
 * - Inheritance: extends JFrame
 * - Abstraction: customer just views comments without knowing how data is loaded
 */
public class ViewComments extends JFrame {

    private CustomerService customerService = new CustomerService();

    public ViewComments(User user) {

        setTitle("My Comments - " + user.getUsername());
        setSize(780, 510);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        setLocationRelativeTo(null);

        // ── Title ──
        JLabel title = new JLabel("My Submitted Comments");
        title.setFont(new Font("Arial", Font.BOLD, 16));
        title.setBounds(275, 15, 280, 30);
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

        JLabel lblHint = new JLabel("(by appointment ID, target name, role, or date)");
        lblHint.setForeground(Color.GRAY);
        lblHint.setFont(new Font("Arial", Font.PLAIN, 11));
        lblHint.setBounds(285, 57, 320, 22);
        add(lblHint);

        // ── Record count ──
        JLabel lblCount = new JLabel("Total: 0");
        lblCount.setBounds(655, 55, 100, 26);
        lblCount.setForeground(Color.DARK_GRAY);
        add(lblCount);

        // ── Table ──
        String[] columns = {"Comment ID", "Appt ID", "Commented To", "Role", "Comment", "Date"};
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        JTable table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        table.setRowHeight(24);

        // Column widths
        int[] colWidths = {90, 75, 110, 100, 240, 90};
        for (int i = 0; i < colWidths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);

        // Color Role column: Technician = blue, CounterStaff = orange
        table.getColumnModel().getColumn(3).setCellRenderer(
            (tbl, value, isSelected, hasFocus, row, col) -> {
                JLabel lbl = new JLabel(value == null ? "" : value.toString());
                lbl.setOpaque(true);
                lbl.setHorizontalAlignment(JLabel.CENTER);
                String role = value == null ? "" : value.toString();
                if (isSelected) {
                    lbl.setBackground(tbl.getSelectionBackground());
                    lbl.setForeground(tbl.getSelectionForeground());
                } else if (role.equalsIgnoreCase("Technician")) {
                    lbl.setBackground(new Color(220, 235, 255));
                    lbl.setForeground(new Color(0, 60, 160));
                } else if (role.equalsIgnoreCase("CounterStaff")) {
                    lbl.setBackground(new Color(255, 245, 220));
                    lbl.setForeground(new Color(150, 90, 0));
                } else {
                    lbl.setBackground(Color.WHITE);
                    lbl.setForeground(Color.BLACK);
                }
                return lbl;
            }
        );

        // Sortable columns
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        // Default sort: Date descending (most recent comment first)
        sorter.setSortKeys(java.util.List.of(
            new RowSorter.SortKey(5, SortOrder.DESCENDING)
        ));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBounds(15, 90, 740, 300);
        add(scrollPane);

        // ── Detail panel: click row to read full comment ──
        JLabel lblDetail = new JLabel("Click a row to read the full comment:");
        lblDetail.setForeground(Color.DARK_GRAY);
        lblDetail.setBounds(15, 398, 260, 20);
        add(lblDetail);

        JTextArea detailArea = new JTextArea();
        detailArea.setEditable(false);
        detailArea.setLineWrap(true);
        detailArea.setWrapStyleWord(true);
        detailArea.setBackground(new Color(250, 250, 245));
        detailArea.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        JScrollPane detailScroll = new JScrollPane(detailArea);
        detailScroll.setBounds(15, 420, 740, 48);
        add(detailScroll);

        // ── Load data ──
        // Comments: [CommentID, AppointmentID, CustomerID, TargetID, TargetRole, Comment, Date]
        ArrayList<String[]> comments = customerService.getCommentsByCustomer(user.getId());

        if (comments.isEmpty()) {
            JLabel noData = new JLabel("You have not submitted any comments yet.");
            noData.setForeground(Color.GRAY);
            noData.setBounds(255, 230, 300, 25);
            add(noData);
        } else {
            for (String[] c : comments) {
                String targetName = customerService.getUsernameById(c[3]);
                tableModel.addRow(new Object[]{
                    c[0],        // Comment ID
                    c[1],        // Appointment ID
                    targetName,  // Target name (resolved from ID)
                    c[4],        // Target role
                    c[5],        // Comment text
                    c[6]         // Date
                });
            }
        }

        lblCount.setText("Total: " + comments.size());

        // ── Row click → show full comment in detail area ──
        table.getSelectionModel().addListSelectionListener(e -> {
            int viewRow = table.getSelectedRow();
            if (viewRow >= 0) {
                int modelRow   = table.convertRowIndexToModel(viewRow);
                String target  = tableModel.getValueAt(modelRow, 2).toString();
                String role    = tableModel.getValueAt(modelRow, 3).toString();
                String comment = tableModel.getValueAt(modelRow, 4).toString();
                String date    = tableModel.getValueAt(modelRow, 5).toString();
                detailArea.setText("[" + date + "] To " + target + " (" + role + "): " + comment);
            }
        });

        // ── Search filter ──
        // Searches: Appt ID(1), Commented To(2), Role(3), Date(5)
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            void filter() {
                String text = txtSearch.getText().trim();
                sorter.setRowFilter(text.isEmpty()
                    ? null
                    : RowFilter.regexFilter("(?i)" + text, 1, 2, 3, 5));
            }
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
        });

        // ── Back ──
        btnBack.addActionListener(e -> {
            dispose();
            new CustomerMenu(user);
        });

        setVisible(true);
    }
}
