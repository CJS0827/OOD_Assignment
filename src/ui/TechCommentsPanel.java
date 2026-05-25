package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import java.awt.*;
import java.util.ArrayList;
import model.User;
import service.TechnicianService;

/**
 * TechCommentsPanel shows all customer comments directed at this technician.
 *
 * OOP Concepts:
 * - Inheritance   : extends JFrame
 * - Encapsulation : TechnicianService handles all file I/O
 * - Abstraction   : technician reads comments without knowing file format
 * - Polymorphism  : isCellEditable() is overridden in the table model
 */
public class TechCommentsPanel extends JFrame {

    private TechnicianService techService = new TechnicianService();

    public TechCommentsPanel(User user) {

        setTitle("Customer Comments - " + user.getUsername());
        setSize(780, 510);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        setLocationRelativeTo(null);

        // Title
        JLabel title = new JLabel("Customer Comments About Me");
        title.setFont(new Font("Arial", Font.BOLD, 16));
        title.setBounds(265, 15, 300, 30);
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

        JLabel lblHint = new JLabel("(by comment ID, appointment ID, customer, or date)");
        lblHint.setForeground(Color.GRAY);
        lblHint.setFont(new Font("Arial", Font.PLAIN, 11));
        lblHint.setBounds(285, 57, 330, 22);
        add(lblHint);

        JLabel lblCount = new JLabel("Total: 0");
        lblCount.setBounds(655, 55, 100, 26);
        lblCount.setForeground(Color.DARK_GRAY);
        add(lblCount);

        // Table
        String[] columns = {"Comment ID", "Appt ID", "From (Customer)", "Comment", "Date"};
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        JTable table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        table.setRowHeight(24);

        int[] colWidths = {90, 75, 130, 310, 95};
        for (int i = 0; i < colWidths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        sorter.setSortKeys(java.util.List.of(new RowSorter.SortKey(4, SortOrder.DESCENDING)));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBounds(15, 90, 740, 290);
        add(scrollPane);

        // Detail area
        JLabel lblDetail = new JLabel("Click a row to read the full comment:");
        lblDetail.setForeground(Color.DARK_GRAY);
        lblDetail.setBounds(15, 388, 280, 20);
        add(lblDetail);

        JTextArea detailArea = new JTextArea();
        detailArea.setEditable(false);
        detailArea.setLineWrap(true);
        detailArea.setWrapStyleWord(true);
        detailArea.setBackground(new Color(250, 250, 245));
        detailArea.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        JScrollPane detailScroll = new JScrollPane(detailArea);
        detailScroll.setBounds(15, 410, 740, 52);
        add(detailScroll);

        // Load data
        ArrayList<String[]> comments = techService.getCommentsForTechnician(user.getId());

        if (comments.isEmpty()) {
            JLabel noData = new JLabel("No comments from customers yet.");
            noData.setForeground(Color.GRAY);
            noData.setBounds(285, 225, 250, 25);
            add(noData);
        } else {
            for (String[] c : comments) {
                // c: CommentID|AppointmentID|CustomerID|TargetID|TargetRole|Comment|Date
                String customerName = techService.getUsernameById(c[2]);
                tableModel.addRow(new Object[]{c[0], c[1], customerName, c[5], c[6]});
            }
        }

        lblCount.setText("Total: " + comments.size());

        // Row click → show full comment
        table.getSelectionModel().addListSelectionListener(e -> {
            int viewRow = table.getSelectedRow();
            if (viewRow >= 0) {
                int mr = table.convertRowIndexToModel(viewRow);
                String customer = tableModel.getValueAt(mr, 2).toString();
                String comment  = tableModel.getValueAt(mr, 3).toString();
                String date     = tableModel.getValueAt(mr, 4).toString();
                detailArea.setText("[" + date + "] From " + customer + ": " + comment);
            }
        });

        // Search filter: comment ID(0), appt ID(1), customer(2), date(4)
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            void filter() {
                String text = txtSearch.getText().trim();
                sorter.setRowFilter(text.isEmpty()
                    ? null
                    : RowFilter.regexFilter("(?i)" + text, 0, 1, 2, 4));
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
