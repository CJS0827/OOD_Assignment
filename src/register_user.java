import javax.swing.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class register_user {

    public register_user() {
        JFrame frame = new JFrame("Register Page");
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(null);

        JLabel titleLabel = new JLabel("User Registration");
        titleLabel.setBounds(130, 20, 150, 25);
        frame.add(titleLabel);

        JLabel userLabel = new JLabel("Username:");
        userLabel.setBounds(50, 70, 80, 25);
        frame.add(userLabel);

        JTextField userText = new JTextField();
        userText.setBounds(150, 70, 180, 25);
        frame.add(userText);

        JLabel passLabel = new JLabel("Password:");
        passLabel.setBounds(50, 110, 80, 25);
        frame.add(passLabel);

        JPasswordField passText = new JPasswordField();
        passText.setBounds(150, 110, 180, 25);
        frame.add(passText);

        JLabel confirmLabel = new JLabel("Confirm:");
        confirmLabel.setBounds(50, 150, 80, 25);
        frame.add(confirmLabel);

        JPasswordField confirmText = new JPasswordField();
        confirmText.setBounds(150, 150, 180, 25);
        frame.add(confirmText);

        JButton registerButton = new JButton("Register");
        registerButton.setBounds(140, 200, 100, 30);
        frame.add(registerButton);

        JButton backButton = new JButton("Back to Login");
        backButton.setBounds(125, 235, 130, 25);
        frame.add(backButton);

        registerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String username = userText.getText().trim();
                String password = new String(passText.getPassword());
                String confirmPassword = new String(confirmText.getPassword());

                if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Please fill in all fields.");
                    return;
                }

                if (!password.equals(confirmPassword)) {
                    JOptionPane.showMessageDialog(frame, "Passwords do not match.");
                    return;
                }

                try {
                    File folder = new File("data");
                    folder.mkdirs();

                    FileWriter writer = new FileWriter("data/users.txt", true);
                    writer.write(username + "," + password + "\n");
                    writer.close();

                    JOptionPane.showMessageDialog(frame, "Registration successful!");

                    frame.dispose();
                    login.main(null);

                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(frame, "Error saving user data.");
                }
            }
        });

        backButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
                login.main(null);
            }
        });

        frame.setVisible(true);
    }
}