package ui;

import model.User;
import service.ManageCustomerService;

import java.util.ArrayList;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import java.awt.event.*;
import java.awt.*;

/**
 * ManageCustomer allows Counter Staff to Create, Read, Update, Delete,
 * Activate/Deactivate customer accounts.
 *
 * OOP Concepts:
 * - Encapsulation: ManageCustomerService hides all file I/O
 * - Inheritance: Uses User model passed from CounterStaffMenu
 * - Polymorphism: User objects stored in ArrayList handled uniformly
 * - Abstraction: UI calls service methods without knowing file structure
 */
public class ManageCustomer {

    private ArrayList<User> allUsers;
    private ArrayList<User> list;           // customers only
    private DefaultTableModel model;
    private ManageCustomerService service = new ManageCustomerService();

    public ManageCustomer(User user) {

        JFrame f = new JFrame("Customer Management");
        f.setSize(860, 580);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setLayout(null);
        f.setLocationRelativeTo(null);

        // ══════════════════════════════════════
        //  TOP BAR: Back + Title
        // ══════════════════════════════════════
        JButton btnBack = new JButton("< Back");
        btnBack.setBounds(15, 12, 80, 28);
        f.add(btnBack);

        JLabel titleLabel = new JLabel("Customer Management");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 15));
        titleLabel.setBounds(320, 12, 250, 28);
        f.add(titleLabel);

        // ══════════════════════════════════════
        //  FORM FIELDS (2 rows x 2 cols)
        // ══════════════════════════════════════
        // Row 1: Username | Password
        JLabel l1 = new JLabel("Username:");
        l1.setBounds(15, 52, 80, 25);
        f.add(l1);
        JTextField tfUsername = new JTextField();
        tfUsername.setBounds(100, 52, 195, 25);
        tfUsername.getDocument().addDocumentListener(fillColorListener(tfUsername));
        f.add(tfUsername);

        JLabel l2 = new JLabel("Password:");
        l2.setBounds(320, 52, 80, 25);
        f.add(l2);
        JPasswordField tfPassword = new JPasswordField();
        tfPassword.setBounds(405, 52, 195, 25);
        f.add(tfPassword);

        JCheckBox chkShowPass = new JCheckBox("Show");
        chkShowPass.setBounds(608, 52, 65, 25);
        chkShowPass.setBackground(f.getBackground());
        f.add(chkShowPass);

        // Row 2: Phone | Email
        JLabel l3 = new JLabel("Phone:");
        l3.setBounds(15, 86, 80, 25);
        f.add(l3);
        JTextField tfPhone = new JTextField();
        tfPhone.setBounds(100, 86, 195, 25);
        tfPhone.getDocument().addDocumentListener(fillColorListener(tfPhone));
        f.add(tfPhone);

        JLabel l4 = new JLabel("Email:");
        l4.setBounds(320, 86, 80, 25);
        f.add(l4);
        JTextField tfEmail = new JTextField();
        tfEmail.setBounds(405, 86, 195, 25);
        tfEmail.getDocument().addDocumentListener(fillColorListener(tfEmail));
        f.add(tfEmail);

        // ── Show/hide password ──
        char defaultEcho = tfPassword.getEchoChar();
        chkShowPass.addActionListener(e ->
            tfPassword.setEchoChar(chkShowPass.isSelected() ? (char) 0 : defaultEcho)
        );

        // ══════════════════════════════════════
        //  ACTION BUTTONS
        // ══════════════════════════════════════
        Color defaultBtnColor = UIManager.getColor("Button.background");

        JButton addBtn        = new JButton("Add");
        JButton updBtn        = new JButton("Update");
        JButton deleteBtn     = new JButton("Delete");
        JButton activateBtn   = new JButton("Activate");
        JButton deactivateBtn = new JButton("Deactivate");
        JButton clearBtn      = new JButton("Clear");

        addBtn       .setBounds(15,  122, 80,  30);
        updBtn       .setBounds(103, 122, 80,  30);
        deleteBtn    .setBounds(191, 122, 80,  30);
        activateBtn  .setBounds(279, 122, 90,  30);
        deactivateBtn.setBounds(377, 122, 100, 30);
        clearBtn     .setBounds(680, 122, 80,  30);

        addBtn       .setBackground(new Color(102, 204, 255));
        updBtn       .setBackground(new Color(255, 204, 102));
        deleteBtn    .setBackground(new Color(220,  80,  80));
        deleteBtn    .setForeground(Color.WHITE);

        // Activate/Deactivate start disabled until a row is selected
        activateBtn  .setEnabled(false);
        deactivateBtn.setEnabled(false);

        f.add(addBtn);
        f.add(updBtn);
        f.add(deleteBtn);
        f.add(activateBtn);
        f.add(deactivateBtn);
        f.add(clearBtn);

        // ══════════════════════════════════════
        //  SEARCH BAR
        // ══════════════════════════════════════
        JLabel lblSearch = new JLabel("Search:");
        lblSearch.setBounds(15, 165, 55, 26);
        f.add(lblSearch);

        JTextField txtSearch = new JTextField();
        txtSearch.setBounds(75, 165, 220, 26);
        f.add(txtSearch);

        JLabel lblHint = new JLabel("(by username, phone, email, or status)");
        lblHint.setForeground(Color.GRAY);
        lblHint.setFont(new Font("Arial", Font.PLAIN, 11));
        lblHint.setBounds(305, 167, 280, 22);
        f.add(lblHint);

        // Record count label (top right of search row)
        JLabel lblCount = new JLabel("Total: 0");
        lblCount.setBounds(680, 165, 120, 26);
        lblCount.setForeground(Color.DARK_GRAY);
        f.add(lblCount);

        // ══════════════════════════════════════
        //  TABLE
        // ══════════════════════════════════════
        model = new DefaultTableModel(
            new String[]{"ID", "Username", "Phone", "Email", "Status"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        table.setRowHeight(24);

        // Column widths
        int[] colWidths = {60, 120, 110, 200, 80};
        for (int i = 0; i < colWidths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);

        // Color Status column: green = Active, red = Inactive
        table.getColumnModel().getColumn(4).setCellRenderer(
            (tbl, value, isSelected, hasFocus, row, col) -> {
                JLabel lbl = new JLabel(value == null ? "" : value.toString());
                lbl.setOpaque(true);
                lbl.setHorizontalAlignment(JLabel.CENTER);
                String status = value == null ? "" : value.toString();
                if (isSelected) {
                    lbl.setBackground(tbl.getSelectionBackground());
                    lbl.setForeground(tbl.getSelectionForeground());
                } else if (status.equalsIgnoreCase("Active")) {
                    lbl.setBackground(new Color(220, 255, 220));
                    lbl.setForeground(new Color(0, 120, 0));
                } else {
                    lbl.setBackground(new Color(255, 220, 220));
                    lbl.setForeground(new Color(160, 0, 0));
                }
                return lbl;
            }
        );

        // Sortable columns
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        // Default sort: Username ascending
        sorter.setSortKeys(java.util.List.of(
            new RowSorter.SortKey(1, SortOrder.ASCENDING)
        ));

        JScrollPane sp = new JScrollPane(table);
        sp.setBounds(15, 200, 820, 300);
        f.add(sp);

        // ══════════════════════════════════════
        //  LOAD DATA
        // ══════════════════════════════════════
        allUsers = service.loadAllUsers();
        list     = service.getCustomers(allUsers);

        for (User u : list) {
            model.addRow(new Object[]{
                u.getId(), u.getUsername(), u.getPhone(), u.getEmail(), u.getStatus()
            });
        }
        lblCount.setText("Total: " + list.size());

        // ══════════════════════════════════════
        //  SEARCH FILTER
        // ══════════════════════════════════════
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            void filter() {
                String text = txtSearch.getText().trim();
                sorter.setRowFilter(text.isEmpty()
                    ? null
                    : RowFilter.regexFilter("(?i)" + text, 1, 2, 3, 4)); // Username, Phone, Email, Status
            }
            public void insertUpdate(DocumentEvent e)  { filter(); }
            public void removeUpdate(DocumentEvent e)  { filter(); }
            public void changedUpdate(DocumentEvent e) { filter(); }
        });

        // ══════════════════════════════════════
        //  CLICK ROW → fill form fields
        //  Must use convertRowIndexToModel so
        //  sorting/filtering doesn't break it
        // ══════════════════════════════════════
        table.getSelectionModel().addListSelectionListener(e -> {
            int viewRow = table.getSelectedRow();
            if (viewRow < 0) return;
            int modelRow = table.convertRowIndexToModel(viewRow);
            User u = list.get(modelRow);

            tfUsername.setText(u.getUsername());
            tfPassword.setText(u.getPassword());
            tfPhone   .setText(u.getPhone());
            tfEmail   .setText(u.getEmail());

            // Update activate/deactivate button states
            boolean canActivate   = u.getStatus().equalsIgnoreCase("Inactive");
            boolean canDeactivate = u.getStatus().equalsIgnoreCase("Active");

            activateBtn  .setEnabled(canActivate);
            deactivateBtn.setEnabled(canDeactivate);
            activateBtn  .setBackground(canActivate   ? new Color(51, 204, 51)  : defaultBtnColor);
            deactivateBtn.setBackground(canDeactivate ? new Color(201, 51, 0)   : defaultBtnColor);
            deactivateBtn.setForeground(canDeactivate ? Color.WHITE             : Color.BLACK);
        });

        // ══════════════════════════════════════
        //  ADD CUSTOMER
        // ══════════════════════════════════════
        addBtn.addActionListener(e -> {
            String username = tfUsername.getText().trim();
            String password = new String(tfPassword.getPassword()).trim();
            String phone    = tfPhone.getText().trim();
            String email    = tfEmail.getText().trim();

            if (username.isEmpty() || password.isEmpty() || phone.isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(f, "Please fill in all fields.");
                return;
            }

            // Validate phone: digits only
            if (!phone.matches("\\d{10,11}")) {
                tfPhone.setBorder(BorderFactory.createLineBorder(Color.RED));
                tfPhone.setBackground(new Color(255, 200, 200));
                JOptionPane.showMessageDialog(f, "Phone must be 10-11 digits.");
                return;
            }

            // Validate email format
            if (!email.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) {
                tfEmail.setBorder(BorderFactory.createLineBorder(Color.RED));
                tfEmail.setBackground(new Color(255, 200, 200));
                JOptionPane.showMessageDialog(f, "Invalid email format.");
                return;
            }

            boolean usernameExists = allUsers.stream()
                .anyMatch(u -> u.getUsername().equalsIgnoreCase(username));
            boolean emailExists = allUsers.stream()
                .anyMatch(u -> u.getEmail().equalsIgnoreCase(email));

            if (usernameExists) {
                tfUsername.setBorder(BorderFactory.createLineBorder(Color.RED));
                tfUsername.setBackground(new Color(255, 200, 200));
                JOptionPane.showMessageDialog(f, "Username already exists!");
                return;
            }
            if (emailExists) {
                tfEmail.setBorder(BorderFactory.createLineBorder(Color.RED));
                tfEmail.setBackground(new Color(255, 200, 200));
                JOptionPane.showMessageDialog(f, "Email already exists!");
                return;
            }

            String id = service.getNextCustomerId(allUsers);
            User customer = new User(id, username, password, phone, email,
                                     "N/A", "N/A", "Customer", "Active");
            allUsers.add(customer);
            list.add(customer);

            model.addRow(new Object[]{id, username, phone, email, "Active"});
            lblCount.setText("Total: " + list.size());
            service.saveAllUsers(allUsers);
            clearFields(tfUsername, tfPassword, tfPhone, tfEmail);
            JOptionPane.showMessageDialog(f, "Customer added successfully!");
        });

        // ══════════════════════════════════════
        //  UPDATE CUSTOMER
        // ══════════════════════════════════════
        updBtn.addActionListener(e -> {
            int viewRow = table.getSelectedRow();
            if (viewRow < 0) {
                JOptionPane.showMessageDialog(f, "Please select a customer to update.");
                return;
            }
            int modelRow = table.convertRowIndexToModel(viewRow);
            User u = list.get(modelRow);

            String newUsername = tfUsername.getText().trim();
            String newPassword = new String(tfPassword.getPassword()).trim();
            String newPhone    = tfPhone.getText().trim();
            String newEmail    = tfEmail.getText().trim();

            if (newUsername.isEmpty() || newPassword.isEmpty() || newPhone.isEmpty() || newEmail.isEmpty()) {
                JOptionPane.showMessageDialog(f, "Please fill in all fields.");
                return;
            }

            // Check username conflict with OTHER users
            boolean usernameConflict = allUsers.stream()
                .anyMatch(other -> !other.getId().equals(u.getId())
                    && other.getUsername().equalsIgnoreCase(newUsername));
            if (usernameConflict) {
                JOptionPane.showMessageDialog(f, "Username already taken by another user.");
                return;
            }

            u.setUsername(newUsername);
            u.setPassword(newPassword);
            u.setPhone(newPhone);
            u.setEmail(newEmail);

            model.setValueAt(newUsername, modelRow, 1);
            model.setValueAt(newPhone,    modelRow, 2);
            model.setValueAt(newEmail,    modelRow, 3);

            service.saveAllUsers(allUsers);
            JOptionPane.showMessageDialog(f, "Customer updated successfully!");
        });

        // ══════════════════════════════════════
        //  DELETE CUSTOMER
        // ══════════════════════════════════════
        deleteBtn.addActionListener(e -> {
            int viewRow = table.getSelectedRow();
            if (viewRow < 0) {
                JOptionPane.showMessageDialog(f, "Please select a customer to delete.");
                return;
            }
            int modelRow = table.convertRowIndexToModel(viewRow);
            User u = list.get(modelRow);

            int confirm = JOptionPane.showConfirmDialog(f,
                "Are you sure you want to delete customer \"" + u.getUsername() + "\"?\nThis cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) return;

            allUsers.remove(u);
            list.remove(modelRow);
            model.removeRow(modelRow);
            lblCount.setText("Total: " + list.size());

            service.saveAllUsers(allUsers);
            clearFields(tfUsername, tfPassword, tfPhone, tfEmail);
            activateBtn  .setEnabled(false);
            deactivateBtn.setEnabled(false);
            activateBtn  .setBackground(defaultBtnColor);
            deactivateBtn.setBackground(defaultBtnColor);
            JOptionPane.showMessageDialog(f, "Customer deleted successfully!");
        });

        // ══════════════════════════════════════
        //  ACTIVATE
        // ══════════════════════════════════════
        activateBtn.addActionListener(e -> {
            int viewRow  = table.getSelectedRow();
            if (viewRow < 0) return;
            int modelRow = table.convertRowIndexToModel(viewRow);
            User u = list.get(modelRow);
            u.setStatus("Active");
            model.setValueAt("Active", modelRow, 4);
            service.saveAllUsers(allUsers);
            activateBtn  .setEnabled(false);
            activateBtn  .setBackground(defaultBtnColor);
            deactivateBtn.setEnabled(true);
            deactivateBtn.setBackground(new Color(201, 51, 0));
            deactivateBtn.setForeground(Color.WHITE);
        });

        // ══════════════════════════════════════
        //  DEACTIVATE
        // ══════════════════════════════════════
        deactivateBtn.addActionListener(e -> {
            int viewRow  = table.getSelectedRow();
            if (viewRow < 0) return;
            int modelRow = table.convertRowIndexToModel(viewRow);
            User u = list.get(modelRow);
            u.setStatus("Inactive");
            model.setValueAt("Inactive", modelRow, 4);
            service.saveAllUsers(allUsers);
            deactivateBtn.setEnabled(false);
            deactivateBtn.setBackground(defaultBtnColor);
            deactivateBtn.setForeground(Color.BLACK);
            activateBtn  .setEnabled(true);
            activateBtn  .setBackground(new Color(51, 204, 51));
        });

        // ══════════════════════════════════════
        //  CLEAR
        // ══════════════════════════════════════
        clearBtn.addActionListener(e -> {
            clearFields(tfUsername, tfPassword, tfPhone, tfEmail);
            table.clearSelection();
            txtSearch.setText("");
            activateBtn  .setEnabled(false);
            deactivateBtn.setEnabled(false);
            activateBtn  .setBackground(defaultBtnColor);
            deactivateBtn.setBackground(defaultBtnColor);
        });

        // ══════════════════════════════════════
        //  BACK
        // ══════════════════════════════════════
        btnBack.addActionListener(e -> {
            f.dispose();
            new CounterStaffMenu(user);
        });

        f.setVisible(true);
    }

    // ── Helpers ──

    /** Clear all form input fields */
    private void clearFields(JTextField username, JPasswordField password,
                              JTextField phone, JTextField email) {
        username.setText("");
        password.setText("");
        phone   .setText("");
        email   .setText("");
        username.setBackground(Color.WHITE);
        password.setBackground(Color.WHITE);
        phone   .setBackground(Color.WHITE);
        email   .setBackground(Color.WHITE);
        username.setBorder(UIManager.getBorder("TextField.border"));
        phone   .setBorder(UIManager.getBorder("TextField.border"));
        email   .setBorder(UIManager.getBorder("TextField.border"));
    }

    /** Yellow highlight while typing, white when empty */
    DocumentListener fillColorListener(JTextField tf) {
        return new DocumentListener() {
            void update() {
                tf.setBackground(tf.getText().isEmpty()
                    ? Color.WHITE
                    : new Color(255, 255, 204));
            }
            public void insertUpdate(DocumentEvent e)  { update(); }
            public void removeUpdate(DocumentEvent e)  { update(); }
            public void changedUpdate(DocumentEvent e) { update(); }
        };
    }
}