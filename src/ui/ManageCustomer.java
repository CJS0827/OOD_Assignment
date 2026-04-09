package ui;

import model.User;
import service.ManageCustomerService;

import java.util.ArrayList;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.event.*;
import java.awt.Color;

public class ManageCustomer {

    private ArrayList<User> allUsers;
    private ArrayList<User> list;

    private DefaultTableModel model;

    private ManageCustomerService service = new ManageCustomerService();

    public ManageCustomer(User user) {

        JFrame f = new JFrame("Customer Management");
        f.setSize(800,420);
        f.setLayout(null);
        f.setLocationRelativeTo(null);


        JButton btnBack = new JButton("< Back");
        btnBack.setBounds(30, 10, 90, 25);
        f.add(btnBack);


        JLabel l1 = new JLabel("Username");
        l1.setBounds(30,40,100,25);
        f.add(l1);

        JTextField tfUsername = new JTextField();
        tfUsername.setBounds(150,40,200,25);
        tfUsername.getDocument().addDocumentListener(fillColorListener(tfUsername));
        f.add(tfUsername);


        JLabel l2 = new JLabel("Password");
        l2.setBounds(380,40,100,25);
        f.add(l2);

        JTextField tfPassword = new JTextField();
        tfPassword.setBounds(500,40,200,25);
        tfPassword.getDocument().addDocumentListener(fillColorListener(tfPassword));
        f.add(tfPassword);


        JLabel l3 = new JLabel("Phone");
        l3.setBounds(30,80,100,25);
        f.add(l3);

        JTextField tfPhone = new JTextField();
        tfPhone.setBounds(150,80,200,25);
        tfPhone.getDocument().addDocumentListener(fillColorListener(tfPhone));
        f.add(tfPhone);


        JLabel l4 = new JLabel("Email");
        l4.setBounds(380,80,100,25);
        f.add(l4);

        JTextField tfEmail = new JTextField();
        tfEmail.setBounds(500,80,200,25);
        tfEmail.getDocument().addDocumentListener(fillColorListener(tfEmail));
        f.add(tfEmail);


        JButton addBtn = new JButton("Add");
        addBtn.setBounds(30, 120, 90, 30);
        addBtn.setBackground(new Color(102,204,255));
        f.add(addBtn);


        JButton updBtn = new JButton("Update");
        updBtn.setBounds(130, 120, 90, 30);
        updBtn.setBackground(new Color(255,204,102));
        f.add(updBtn);


        JButton activateBtn = new JButton("Activate");
        activateBtn.setBounds(230, 120, 100, 30);
        activateBtn.setBackground(new Color(51,204,51));
        f.add(activateBtn);


        JButton deactivateBtn = new JButton("Deactivate");
        deactivateBtn.setBounds(340, 120, 110, 30);
        deactivateBtn.setBackground(new Color(201,51,0));
        deactivateBtn.setForeground(Color.WHITE);
        f.add(deactivateBtn);
        
        Color defaultColor = UIManager.getColor("Button.background");

        JButton clearBtn = new JButton("Clear");
        clearBtn.setBounds(560, 120, 90, 30);
        f.add(clearBtn);


        model = new DefaultTableModel();

        model.setColumnIdentifiers(
                new String[]{"ID","Username","Phone","Email","Status"}
        );


        JTable table = new JTable(model);

        JScrollPane sp = new JScrollPane(table);

        sp.setBounds(30,160,720,180);

        f.add(sp);


        // load from service
        allUsers = service.loadAllUsers();

        list = service.getCustomers(allUsers);


        for(User u : list){

            model.addRow(new Object[]{

                    u.getId(),
                    u.getUsername(),
                    u.getPhone(),
                    u.getEmail(),
                    u.getStatus()

            });

        }

     // ADD CUSTOMER
        addBtn.addActionListener(e -> {

            String username = tfUsername.getText().trim();
            String password = tfPassword.getText().trim();
            String phone = tfPhone.getText().trim();
            String email = tfEmail.getText().trim();

            if(username.isEmpty() || password.isEmpty() || phone.isEmpty() || email.isEmpty()){
                JOptionPane.showMessageDialog(f, "Please fill all fields");
                return;
            }

            boolean usernameExists = allUsers.stream()
                    .anyMatch(u -> u.getUsername().equalsIgnoreCase(username));
            boolean emailExists = allUsers.stream()
                    .anyMatch(u -> u.getEmail().equalsIgnoreCase(email));

            if(usernameExists){
                tfUsername.setBorder(BorderFactory.createLineBorder(Color.RED));
                tfUsername.setBackground(new Color(255,200,200)); // light red background
                JOptionPane.showMessageDialog(f, "Username already exists! Customer not added.");
                return;
            } else {
                tfUsername.setBorder(UIManager.getBorder("TextField.border"));
                tfUsername.setBackground(new Color(255,255,204)); // yellow for typing
            }
            
            if(emailExists){
                tfEmail.setBorder(BorderFactory.createLineBorder(Color.RED));
                tfEmail.setBackground(new Color(255,200,200)); // light red background
                JOptionPane.showMessageDialog(f, "Email already exists! Customer not added.");
                return;
            } else {
                tfEmail.setBorder(UIManager.getBorder("TextField.border"));
                tfEmail.setBackground(new Color(255,255,204)); // yellow for typing
            }
            

            String id = service.getNextCustomerId(allUsers);

            User customer = new User(
                    id,
                    username,
                    password,
                    phone,
                    email,
                    "N/A",  // Security question
                    "N/A",  // Security answer
                    "Customer",
                    "Active"
            );

            allUsers.add(customer);
            list.add(customer);

            model.addRow(new Object[]{
                    id,
                    username,
                    phone,
                    email,
                    "Active"
            });

            service.saveAllUsers(allUsers);

            JOptionPane.showMessageDialog(f, "Customer Added Successfully!");
        });



        // CLICK TABLE
        table.addMouseListener(new MouseAdapter(){

            public void mouseClicked(MouseEvent e){

                int i = table.getSelectedRow();

                tfUsername.setText(
                        list.get(i).getUsername()
                );

                tfPassword.setText(
                        list.get(i).getPassword()
                );

                tfPhone.setText(
                        list.get(i).getPhone()
                );

                tfEmail.setText(
                        list.get(i).getEmail()
                );

            }

        });



        // UPDATE CUSTOMER
        updBtn.addActionListener(e -> {

            int i = table.getSelectedRow();

            if(i < 0)
                return;


            User u = list.get(i);


            u.setUsername(tfUsername.getText());
            u.setPassword(tfPassword.getText());
            u.setPhone(tfPhone.getText());
            u.setEmail(tfEmail.getText());


            model.setValueAt(
                    u.getUsername(),
                    i,
                    1
            );

            model.setValueAt(
                    u.getPhone(),
                    i,
                    2
            );

            model.setValueAt(
                    u.getEmail(),
                    i,
                    3
            );


            service.saveAllUsers(allUsers);


            JOptionPane.showMessageDialog(
                    f,
                    "Customer Updated"
            );

        });



        // ACTIVATE
        activateBtn.addActionListener(e -> {

            int i = table.getSelectedRow();

            if(i < 0)
                return;


            User u = list.get(i);

            u.setStatus("Active");


            model.setValueAt(
                    "Active",
                    i,
                    4
            );


            service.saveAllUsers(allUsers);

        });



        // DEACTIVATE
        deactivateBtn.addActionListener(e -> {

            int i = table.getSelectedRow();

            if(i < 0)
                return;


            User u = list.get(i);

            u.setStatus("Inactive");


            model.setValueAt(
                    "Inactive",
                    i,
                    4
            );


            service.saveAllUsers(allUsers);

        });

        activateBtn.setEnabled(false);

        deactivateBtn.setEnabled(false);
        
        activateBtn.setBackground(defaultColor);
        
        deactivateBtn.setBackground(defaultColor);

        table.getSelectionModel().addListSelectionListener(e -> {

            int i = table.getSelectedRow();

            if(i >= 0){

                User u = list.get(i);

                boolean canActivate =
                        !u.getStatus().equalsIgnoreCase("Active");

                boolean canDeactivate =
                        !u.getStatus().equalsIgnoreCase("Inactive");


                activateBtn.setEnabled(canActivate);
                deactivateBtn.setEnabled(canDeactivate);

                activateBtn.setBackground(
                        canActivate
                                ? new Color(51,204,51)
                                : defaultColor
                );


                deactivateBtn.setBackground(
                        canDeactivate
                                ? new Color(201,51,0)
                                : defaultColor
                );

            }

        });



        // CLEAR
        clearBtn.addActionListener(e -> {

            tfUsername.setText("");
            tfPassword.setText("");
            tfPhone.setText("");
            tfEmail.setText("");

            table.clearSelection();

        });



        // BACK
        btnBack.addActionListener(e -> {

            f.dispose();

            new CounterStaffMenu(user);

        });



        f.setVisible(true);

    }



    // change field color when typing
    DocumentListener fillColorListener(JTextField tf){

        return new DocumentListener(){

            void update(){

                if(tf.getText().isEmpty())

                    tf.setBackground(Color.WHITE);

                else

                    tf.setBackground(
                            new Color(255,255,204)
                    );

            }

            public void insertUpdate(DocumentEvent e){
                update();
            }

            public void removeUpdate(DocumentEvent e){
                update();
            }

            public void changedUpdate(DocumentEvent e){
                update();
            }

        };

    }

}