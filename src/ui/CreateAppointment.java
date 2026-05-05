package ui;

import model.User;
import service.AppointmentService;

import javax.swing.*;
import java.awt.Color;
import com.toedter.calendar.JDateChooser;
import java.text.SimpleDateFormat;

public class CreateAppointment {

    private JComboBox<String> customerBox;
    private JComboBox<String> serviceBox;
    private JComboBox<String> technicianBox;
    private JComboBox<String> timeBox;

    private JDateChooser dateChooser;
    private JTextField plateField;
    private JTextField modelField;

    private JLabel durationLabel;

    private String[] customerEntries;
    private String[] technicianEntries;

    private AppointmentService service = new AppointmentService();

    public CreateAppointment(User user) {

        JFrame f = new JFrame("Create Appointment");
        f.setSize(420, 480);
        f.setLayout(null);
        f.setLocationRelativeTo(null);

        JLabel title = new JLabel("Create Appointment");
        title.setBounds(140, 10, 200, 25);
        f.add(title);

        // --- Customer ---
        JLabel l1 = new JLabel("Customer:");
        l1.setBounds(30, 50, 100, 25);
        f.add(l1);

        customerEntries = service.loadCustomers(); // "U005|Elephant", ...
        customerBox = new JComboBox<>(extractNames(customerEntries));
        customerBox.setBounds(150, 50, 200, 25);
        f.add(customerBox);

        // --- Service Type ---
        JLabel l2 = new JLabel("Service Type:");
        l2.setBounds(30, 85, 100, 25);
        f.add(l2);

        serviceBox = new JComboBox<>(new String[]{"Normal Service", "Major Service"});
        serviceBox.setBounds(150, 85, 200, 25);
        f.add(serviceBox);

        // --- Technician ---
        JLabel l3 = new JLabel("Technician:");
        l3.setBounds(30, 120, 100, 25);
        f.add(l3);

        technicianEntries = service.loadTechnicians(); // "U003|Cat", ...
        technicianBox = new JComboBox<>(extractNames(technicianEntries));
        technicianBox.setBounds(150, 120, 200, 25);
        f.add(technicianBox);

        // --- Date ---
        JLabel l4 = new JLabel("Date:");
        l4.setBounds(30, 155, 100, 25);
        f.add(l4);

        dateChooser = new JDateChooser();
        dateChooser.setDate(new java.util.Date()); // defaults to today
        dateChooser.setDateFormatString("yyyy-MM-dd");
        dateChooser.setBounds(150, 155, 200, 25);
        f.add(dateChooser);

        // --- Time ---
        JLabel l5 = new JLabel("Start Time:");
        l5.setBounds(30, 190, 100, 25);
        f.add(l5);

        timeBox = new JComboBox<>(new String[]{
                "09:00", "10:00", "11:00", "12:00", "14:00", "15:00", "16:00"
        });
        timeBox.setBounds(150, 190, 200, 25);
        f.add(timeBox);

        // --- Duration (auto) ---
        JLabel l6 = new JLabel("Duration:");
        l6.setBounds(30, 225, 100, 25);
        f.add(l6);

        durationLabel = new JLabel("1 hour");
        durationLabel.setBounds(150, 225, 200, 25);
        f.add(durationLabel);

        // --- Car Plate ---
        JLabel l7 = new JLabel("Car Plate:");
        l7.setBounds(30, 260, 100, 25);
        f.add(l7);

        plateField = new JTextField();
        plateField.setBounds(150, 260, 200, 25);
        f.add(plateField);

        // --- Vehicle Model ---
        JLabel l8 = new JLabel("Vehicle Model:");
        l8.setBounds(30, 295, 110, 25);
        f.add(l8);

        modelField = new JTextField();
        modelField.setBounds(150, 295, 200, 25);
        f.add(modelField);

        // --- Buttons ---
        JButton backBtn = new JButton("< Back");
        backBtn.setBounds(10, 10, 80, 25);
        f.add(backBtn);

        JButton assignBtn = new JButton("Assign Appointment");
        assignBtn.setBounds(120, 345, 170, 30);
        f.add(assignBtn);

        // Auto-update duration label
        serviceBox.addActionListener(e -> {
            String selected = serviceBox.getSelectedItem().toString();
            durationLabel.setText(selected.equals("Normal Service") ? "1 hour" : "3 hours");
        });

        // Assign button
        assignBtn.addActionListener(e -> {

        	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        	String date = dateChooser.getDate() != null ? sdf.format(dateChooser.getDate()) : "";
            String plate = plateField.getText().trim();
            String model = modelField.getText().trim();
            String time  = timeBox.getSelectedItem().toString();
            String serviceType = serviceBox.getSelectedItem().toString();

            // Reset borders
            dateChooser.setBorder(UIManager.getBorder("TextField.border"));
            plateField.setBorder(UIManager.getBorder("TextField.border"));
            modelField.setBorder(UIManager.getBorder("TextField.border"));

            boolean valid = true;

            if (customerEntries.length == 0) {
                JOptionPane.showMessageDialog(f, "No active customers found.");
                return;
            }
            if (technicianEntries.length == 0) {
                JOptionPane.showMessageDialog(f, "No active technicians found.");
                return;
            }
            if (date.isEmpty()) {
                dateChooser.setBorder(BorderFactory.createLineBorder(Color.RED));
                valid = false;
            }
            if (plate.isEmpty()) {
                plateField.setBorder(BorderFactory.createLineBorder(Color.RED));
                valid = false;
            }
            if (model.isEmpty()) {
                modelField.setBorder(BorderFactory.createLineBorder(Color.RED));
                valid = false;
            }
            if (!valid) {
                JOptionPane.showMessageDialog(f, "Please fill in all required fields.");
                return;
            }

            // Extract IDs from selected entries
            String customerID   = extractId(customerEntries, customerBox.getSelectedIndex());
            String technicianID = extractId(technicianEntries, technicianBox.getSelectedIndex());
            int duration        = serviceType.equals("Normal Service") ? 1 : 3;
            String appointmentID = service.generateAppointmentID();

            service.saveAppointment(
                    appointmentID,
                    customerID,      // saves U005, not "Elephant"
                    serviceType,
                    technicianID,    // saves U003, not "Cat"
                    date,
                    time,
                    duration,
                    plate,
                    model
            );

            JOptionPane.showMessageDialog(f, "Appointment created successfully!\nID: " + appointmentID);
            f.dispose();
            new CounterStaffMenu(user);
        });

        backBtn.addActionListener(e -> {
            f.dispose();
            new CounterStaffMenu(user);
        });

        f.setVisible(true);
    }

    // Extract just the display names from "ID|Name" entries
    private String[] extractNames(String[] entries) {
        String[] names = new String[entries.length];
        for (int i = 0; i < entries.length; i++) {
            String[] parts = entries[i].split("\\|");
            names[i] = parts.length >= 2 ? parts[1] : parts[0];
        }
        return names;
    }

    // Get the ID part from an "ID|Name" entry by index
    private String extractId(String[] entries, int index) {
        if (index < 0 || index >= entries.length) return "";
        String[] parts = entries[index].split("\\|");
        return parts[0];
    }
}