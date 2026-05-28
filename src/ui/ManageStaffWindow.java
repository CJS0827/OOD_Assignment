package ui;

import model.User;
import model.Manager;
import service.StaffManagementService;
import model.InvalidInputException;
import model.DuplicateRecordException;

import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;

// GUI & Events: Swing components with ActionListeners (OODJ Principle)
public class ManageStaffWindow {

    private ArrayList<User> allUsers;
    private ArrayList<User> staffList;
    private DefaultTableModel tableModel;
    // Encapsulation: private service instance
    private StaffManagementService service = new StaffManagementService();

    public ManageStaffWindow(User manager) {

        JFrame frame = new JFrame("Manage Staff");
        frame.setSize(850, 520);
        frame.setLayout(null);
        frame.setLocationRelativeTo(null);

        // Back button
        JButton btnBack = new JButton("< Back");
        btnBack.setBounds(30, 10, 90, 25);
        frame.add(btnBack);

        // Input fields with labels
        JLabel lblUsername = new JLabel("Username:");
        lblUsername.setBounds(30, 45, 100, 25);
        frame.add(lblUsername);

        JTextField tfUsername = new JTextField();
        tfUsername.setBounds(130, 45, 150, 25);
        frame.add(tfUsername);

        JLabel lblPassword = new JLabel("Password:");
        lblPassword.setBounds(300, 45, 100, 25);
        frame.add(lblPassword);

        JTextField tfPassword = new JTextField();
        tfPassword.setBounds(400, 45, 150, 25);
        frame.add(tfPassword);

        JLabel lblPhone = new JLabel("Phone:");
        lblPhone.setBounds(30, 80, 100, 25);
        frame.add(lblPhone);

        JTextField tfPhone = new JTextField();
        tfPhone.setBounds(130, 80, 150, 25);
        frame.add(tfPhone);

        JLabel lblEmail = new JLabel("Email:");
        lblEmail.setBounds(300, 80, 100, 25);
        frame.add(lblEmail);

        JTextField tfEmail = new JTextField();
        tfEmail.setBounds(400, 80, 150, 25);
        frame.add(tfEmail);

        JLabel lblRole = new JLabel("Role:");
        lblRole.setBounds(570, 45, 50, 25);
        frame.add(lblRole);

        // GUI: JComboBox for role selection
        String[] roles = {"Technician", "CounterStaff", "Manager"};
        JComboBox<String> cmbRole = new JComboBox<>(roles);
        cmbRole.setBounds(620, 45, 150, 25);
        frame.add(cmbRole);

        // Action buttons with color coding
        JButton btnAdd = new JButton("Add");
        btnAdd.setBounds(30, 120, 90, 30);
        btnAdd.setBackground(new Color(102, 204, 255));
        frame.add(btnAdd);

        JButton btnUpdate = new JButton("Update");
        btnUpdate.setBounds(130, 120, 100, 30);
        btnUpdate.setBackground(new Color(255, 204, 102));
        frame.add(btnUpdate);

        JButton btnActivate = new JButton("Activate");
        btnActivate.setBounds(240, 120, 100, 30);
        btnActivate.setBackground(new Color(51, 204, 51));
        frame.add(btnActivate);

        JButton btnDeactivate = new JButton("Deactivate");
        btnDeactivate.setBounds(350, 120, 120, 30);
        btnDeactivate.setBackground(new Color(201, 51, 0));
        btnDeactivate.setForeground(Color.WHITE);
        frame.add(btnDeactivate);

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.setBounds(580, 120, 90, 30);
        frame.add(btnRefresh);

        JButton btnClear = new JButton("Clear");
        btnClear.setBounds(680, 120, 90, 30);
        frame.add(btnClear);

        // JTable to display staff list (GUI & Events principle)
        tableModel = new DefaultTableModel();
        tableModel.setColumnIdentifiers(
            new String[]{"ID", "Username", "Role", "Phone", "Email", "Status"}
        );

        JTable table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBounds(30, 165, 780, 280);
        frame.add(scrollPane);

        // Load and display staff data
        loadStaffData();

        Color defaultBtnColor = UIManager.getColor("Button.background");

        // Initially disable activate/deactivate until a row is selected
        btnActivate.setEnabled(false);
        btnDeactivate.setEnabled(false);
        btnActivate.setBackground(defaultBtnColor);
        btnDeactivate.setBackground(defaultBtnColor);

        // ActionListener: Add new staff (CRUD - Create)
        // Demonstrates handling multiple custom exceptions (Lecture 9.0)
        btnAdd.addActionListener(e -> {
            String username = tfUsername.getText().trim();
            String password = tfPassword.getText().trim();
            String phone    = tfPhone.getText().trim();
            String email    = tfEmail.getText().trim();
            String role     = cmbRole.getSelectedItem().toString();

            if (username.isEmpty() || password.isEmpty() || phone.isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Please fill in all fields.");
                return;
            }

            try {
                allUsers  = service.loadAllUsers();  // reload fresh
                staffList = service.getStaffList(allUsers);

                // ✅ Check phone duplicate across ALL users
                boolean phoneExists = allUsers.stream()
                    .anyMatch(u -> u.getPhone().equals(phone));
                if (phoneExists) {
                    JOptionPane.showMessageDialog(frame,
                        "Phone number already registered by another user.",
                        "Duplicate Record", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // ✅ Check email duplicate across ALL users
                boolean emailExists = allUsers.stream()
                    .anyMatch(u -> u.getEmail().equalsIgnoreCase(email));
                if (emailExists) {
                    JOptionPane.showMessageDialog(frame,
                        "Email already registered by another user.",
                        "Duplicate Record", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                User newStaff = service.addStaff(username, password, phone, email, role, allUsers);
                staffList.add(newStaff);

                tableModel.addRow(new Object[]{
                    newStaff.getId(), newStaff.getUsername(), newStaff.getRole(),
                    newStaff.getPhone(), newStaff.getEmail(), newStaff.getStatus()
                });

                JOptionPane.showMessageDialog(frame, "Staff added successfully!");
                clearFields(tfUsername, tfPassword, tfPhone, tfEmail);

            } catch (InvalidInputException ex) {
                JOptionPane.showMessageDialog(frame,
                    "Invalid " + ex.getFieldName() + ": " + ex.getMessage(),
                    "Validation Error", JOptionPane.WARNING_MESSAGE);

            } catch (DuplicateRecordException ex) {
                JOptionPane.showMessageDialog(frame,
                    ex.getMessage(),
                    "Duplicate Record", JOptionPane.WARNING_MESSAGE);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame,
                    "Unexpected error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // ActionListener: Click table row to populate fields
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    tfUsername.setText(staffList.get(row).getUsername());
                    tfPassword.setText(staffList.get(row).getPassword());
                    tfPhone.setText(staffList.get(row).getPhone());
                    tfEmail.setText(staffList.get(row).getEmail());
                    // Set role combo box to match selected staff
                    cmbRole.setSelectedItem(staffList.get(row).getRole());
                }
            }
        });

     // ActionListener: Update staff details (CRUD - Update)
        btnUpdate.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(frame, "Please select a staff member to update.");
                return;
            }

            try {
                String newPhone = tfPhone.getText().trim();
                String newEmail = tfEmail.getText().trim();

                if (!Manager.isValidPhone(newPhone)) {
                    JOptionPane.showMessageDialog(frame, "Invalid phone number.\nPhone must be 10–12 digits.");
                    return;
                }
                if (!Manager.isValidEmail(newEmail)) {
                    JOptionPane.showMessageDialog(frame, "Invalid email address.\nExample: name@domain.com");
                    return;
                }

                User staff = staffList.get(row);

                // ✅ Check phone conflict with OTHER users
                boolean phoneConflict = allUsers.stream()
                    .anyMatch(u -> !u.getId().equals(staff.getId())
                        && u.getPhone().equals(newPhone));
                if (phoneConflict) {
                    JOptionPane.showMessageDialog(frame,
                        "Phone number already registered by another user.",
                        "Duplicate Record", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // ✅ Check email conflict with OTHER users
                boolean emailConflict = allUsers.stream()
                    .anyMatch(u -> !u.getId().equals(staff.getId())
                        && u.getEmail().equalsIgnoreCase(newEmail));
                if (emailConflict) {
                    JOptionPane.showMessageDialog(frame,
                        "Email already registered by another user.",
                        "Duplicate Record", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                staff.setPhone(newPhone);
                staff.setEmail(newEmail);

                tableModel.setValueAt(staff.getPhone(), row, 3);
                tableModel.setValueAt(staff.getEmail(), row, 4);

                service.saveAllUsers(allUsers);
                JOptionPane.showMessageDialog(frame, "Staff updated successfully!");

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Error updating staff: " + ex.getMessage());
            }
        });

        // ActionListener: Activate staff (CRUD - Update status)
        btnActivate.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) return;

            User staff = staffList.get(row);
            staff.setStatus("Active");
            tableModel.setValueAt("Active", row, 5);
            service.saveAllUsers(allUsers);
            JOptionPane.showMessageDialog(frame,
                "Staff member '" + staff.getUsername() + "' has been activated.");
        });

        // ActionListener: Deactivate staff (logical delete - CRUD Delete)
        // Asks for confirmation before destructive action (UX best practice)
        btnDeactivate.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) return;

            User staff = staffList.get(row);

            // Confirmation dialog: prevent accidental deactivation
            int choice = JOptionPane.showConfirmDialog(
                frame,
                "Are you sure you want to deactivate staff member '" + staff.getUsername() + "'?\n\n"
                    + "The user will no longer be able to log in.\n"
                    + "You can re-activate them later if needed.",
                "Confirm Deactivation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );

            if (choice != JOptionPane.YES_OPTION) {
                return; // user cancelled
            }

            staff.setStatus("Inactive");
            tableModel.setValueAt("Inactive", row, 5);
            service.saveAllUsers(allUsers);
            JOptionPane.showMessageDialog(frame,
                "Staff member '" + staff.getUsername() + "' has been deactivated.");
        });

        // Enable/disable activate/deactivate based on selection
        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                User staff = staffList.get(row);
                boolean canActivate = !staff.getStatus().equalsIgnoreCase("Active");
                boolean canDeactivate = !staff.getStatus().equalsIgnoreCase("Inactive");

                btnActivate.setEnabled(canActivate);
                btnDeactivate.setEnabled(canDeactivate);
                btnActivate.setBackground(canActivate ? new Color(51, 204, 51) : defaultBtnColor);
                btnDeactivate.setBackground(canDeactivate ? new Color(201, 51, 0) : defaultBtnColor);
            }
        });

        // ActionListener: Refresh table data
        btnRefresh.addActionListener(e -> {
            loadStaffData();
            clearFields(tfUsername, tfPassword, tfPhone, tfEmail);
            table.clearSelection();
        });

        // ActionListener: Clear input fields
        btnClear.addActionListener(e -> {
            clearFields(tfUsername, tfPassword, tfPhone, tfEmail);
            table.clearSelection();
        });

        // ActionListener: Back to Manager Menu
        btnBack.addActionListener(e -> {
            frame.dispose();
            new ManagerMenu(manager);
        });

        frame.setVisible(true);
    }

    // Abstraction: Method to load staff data from file into table
    private void loadStaffData() {
        allUsers = service.loadAllUsers();
        staffList = service.getStaffList(allUsers);

        // Clear existing table rows
        tableModel.setRowCount(0);

        // Populate JTable with staff data
        for (User u : staffList) {
            tableModel.addRow(new Object[]{
                u.getId(),
                u.getUsername(),
                u.getRole(),
                u.getPhone(),
                u.getEmail(),
                u.getStatus()
            });
        }
    }

    // Helper method to clear text fields
    private void clearFields(JTextField... fields) {
        for (JTextField field : fields) {
            field.setText("");
        }
    }
}
