package ui;

import javax.swing.*;
import model.User;

public class TechnicianMenu {

    public TechnicianMenu(User user) {

        JFrame frame = new JFrame("Technician Menu");
        frame.setSize(450, 350);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(null);
        frame.setLocationRelativeTo(null);

        // Welcome Label
        JLabel welcomeLabel = new JLabel("Welcome Technician, " + user.getUsername());
        welcomeLabel.setBounds(100, 30, 300, 25);
        frame.add(welcomeLabel);

        // Buttons
        JButton btnProfile = new JButton("Edit Profile");
        btnProfile.setBounds(130, 80, 180, 30);
        frame.add(btnProfile);

        JButton btnAppointments = new JButton("My Appointments");
        btnAppointments.setBounds(130, 120, 180, 30);
        frame.add(btnAppointments);

        JButton btnFeedback = new JButton("Provide Feedback");
        btnFeedback.setBounds(130, 160, 180, 30);
        frame.add(btnFeedback);

        JButton btnComments = new JButton("View Comments");
        btnComments.setBounds(130, 200, 180, 30);
        frame.add(btnComments);

        JButton btnLogout = new JButton("Logout");
        btnLogout.setBounds(130, 250, 180, 30);
        frame.add(btnLogout);

        btnProfile.addActionListener(e -> {
            frame.dispose();
            new EditProfile(user);
        });

        btnAppointments.addActionListener(e -> {
            frame.dispose();
            new TechAppointmentsPanel(user);
        });

        btnFeedback.addActionListener(e -> {
            frame.dispose();
            new TechFeedbackPanel(user);
        });

        btnComments.addActionListener(e -> {
            frame.dispose();
            new TechCommentsPanel(user);
        });

        btnLogout.addActionListener(e -> {
            frame.dispose();
            new LoginPage();
        });

        frame.setVisible(true);
    }
}
