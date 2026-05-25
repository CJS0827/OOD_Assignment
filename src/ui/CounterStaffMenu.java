package ui;
import javax.swing.*;
import model.User;
public class CounterStaffMenu {
    public CounterStaffMenu(User user) {
        JFrame frame = new JFrame("Counter Staff Menu");
        frame.setSize(450, 450);  // increased height
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(null);
        frame.setLocationRelativeTo(null);

        JLabel welcomeLabel = new JLabel("Welcome Counter Staff, " + user.getUsername());
        welcomeLabel.setBounds(90, 30, 300, 25);
        frame.add(welcomeLabel);

        JButton btnProfile     = new JButton("Edit Profile");
        JButton btnCustomer    = new JButton("Manage Customers");
        JButton btnAppointment = new JButton("Create Appointment");
        JButton btnManageAppt  = new JButton("Manage Appointments");  // new
        JButton btnPayment     = new JButton("Payment & Receipt");
        JButton btnRecords     = new JButton("View Payment Records");
        JButton btnLogout      = new JButton("Logout");

        btnProfile.setBounds(130, 70, 180, 30);
        btnCustomer.setBounds(130, 110, 180, 30);
        btnAppointment.setBounds(130, 150, 180, 30);
        btnManageAppt.setBounds(130, 190, 180, 30);   // new
        btnPayment.setBounds(130, 230, 180, 30);
        btnRecords.setBounds(130, 270, 180, 30);
        btnLogout.setBounds(130, 310, 180, 30);

        frame.add(btnProfile);
        frame.add(btnCustomer);
        frame.add(btnAppointment);
        frame.add(btnManageAppt);  // new
        frame.add(btnPayment);
        frame.add(btnRecords);
        frame.add(btnLogout);

        btnProfile.addActionListener(e -> { frame.dispose(); new EditProfile(user); });
        btnCustomer.addActionListener(e -> { frame.dispose(); new ManageCustomer(user); });
        btnAppointment.addActionListener(e -> { frame.dispose(); new CreateAppointment(user); });
        btnManageAppt.addActionListener(e -> { frame.dispose(); new ManageAppointments(user); });  // new
        btnPayment.addActionListener(e -> { frame.dispose(); new CollectPayment(user); });
        btnRecords.addActionListener(e -> { frame.dispose(); new PaymentRecords(user); });
        btnLogout.addActionListener(e -> { frame.dispose(); new LoginPage(); });

        frame.setVisible(true);
    }
}