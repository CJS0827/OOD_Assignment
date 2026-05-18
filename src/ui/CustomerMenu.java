package ui;

import javax.swing.*;
import java.awt.*;
import model.User;

/**
 * CustomerMenu is the main dashboard for a logged-in Customer.
 *
 * OOP Concepts demonstrated:
 * - Encapsulation: User object passed in; only getUsername() used here
 * - Polymorphism: User object is the same type used by all roles (Manager, Staff, etc.)
 *   but this menu is shown specifically when role == "Customer"
 * - Abstraction: Each button opens a separate class that hides its own logic
 */
public class CustomerMenu extends JFrame {

    public CustomerMenu(User user) {

        setTitle("Customer Menu");
        setSize(450, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        setLocationRelativeTo(null);

        // ── Welcome label ──
        JLabel welcomeLabel = new JLabel("Welcome, " + user.getUsername() + "!");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 14));
        welcomeLabel.setBounds(120, 25, 280, 30);
        add(welcomeLabel);

        JLabel roleLabel = new JLabel("[ Customer ]");
        roleLabel.setForeground(Color.GRAY);
        roleLabel.setBounds(180, 50, 120, 20);
        add(roleLabel);

        // ── Buttons ──
        JButton btnProfile  = new JButton("Edit Profile");
        JButton btnService  = new JButton("Service History");
        JButton btnPayment  = new JButton("Payment History");
        JButton btnFeedback = new JButton("View Technician Feedback");
        JButton btnComment  = new JButton("Add Comment");
        JButton btnMyComment = new JButton("My Comments");
        JButton btnLogout   = new JButton("Logout");

        // Position buttons
        int btnX = 100, btnW = 240, btnH = 35;
        btnProfile .setBounds(btnX,  85, btnW, btnH);
        btnService .setBounds(btnX, 130, btnW, btnH);
        btnPayment .setBounds(btnX, 175, btnW, btnH);
        btnFeedback.setBounds(btnX, 220, btnW, btnH);
        btnComment  .setBounds(btnX, 265, btnW, btnH);
        btnMyComment.setBounds(btnX, 310, btnW, btnH);
        btnLogout   .setBounds(btnX, 358, btnW, 28);

        add(btnProfile);
        add(btnService);
        add(btnPayment);
        add(btnFeedback);
        add(btnComment);
        add(btnMyComment);
        add(btnLogout);

        // ── Actions ──
        btnProfile.addActionListener(e -> {
            dispose();
            new EditProfile(user);
        });

        btnService.addActionListener(e -> {
            dispose();
            new ServiceHistory(user);
        });

        btnPayment.addActionListener(e -> {
            dispose();
            new PaymentHistory(user);
        });

        btnFeedback.addActionListener(e -> {
            dispose();
            new ViewFeedback(user);
        });

        btnComment.addActionListener(e -> {
            dispose();
            new AddComment(user);
        });

        btnMyComment.addActionListener(e -> {
            dispose();
            new ViewComments(user);
        });

        btnLogout.addActionListener(e -> {
            dispose();
            new LoginPage();
        });

        setVisible(true);
    }
}
