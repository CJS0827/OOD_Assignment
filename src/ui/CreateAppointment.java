package ui;

import model.User;
import model.Appointment;
import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;

public class CreateAppointment {

    private final String customerFile = "data/users.txt";
    private final String technicianFile = "data/users.txt";
    private final String appointmentFile = "data/appointments.txt";

    private JComboBox<String> customerBox;
    private JComboBox<String> serviceBox;
    private JComboBox<String> technicianBox;
    private JComboBox<String> timeBox;
    private JTextField dateField;
    private JLabel durationLabel;

    public CreateAppointment(User user) {

        JFrame f = new JFrame("Create Appointment");
        f.setSize(420, 350);
        f.setLayout(null);
        f.setLocationRelativeTo(null);

        JLabel title = new JLabel("Create Appointment");
        title.setBounds(140, 10, 150, 25);
        f.add(title);

        JLabel l1 = new JLabel("Customer:");
        l1.setBounds(30, 50, 100, 25);
        f.add(l1);

        customerBox = new JComboBox<>(loadCustomers());
        customerBox.setBounds(150, 50, 200, 25);
        f.add(customerBox);

        JLabel l2 = new JLabel("Service Type:");
        l2.setBounds(30, 80, 100, 25);
        f.add(l2);

        String[] services = {"Normal Service", "Major Service"};
        serviceBox = new JComboBox<>(services);
        serviceBox.setBounds(150, 80, 200, 25);
        f.add(serviceBox);

        JLabel l3 = new JLabel("Technician:");
        l3.setBounds(30, 110, 100, 25);
        f.add(l3);

        technicianBox = new JComboBox<>(loadTechnicians());
        technicianBox.setBounds(150, 110, 200, 25);
        f.add(technicianBox);

        JLabel l4 = new JLabel("Date:");
        l4.setBounds(30, 140, 100, 25);
        f.add(l4);

        dateField = new JTextField("2026-04-10");
        dateField.setBounds(150, 140, 200, 25);
        f.add(dateField);

        JLabel l5 = new JLabel("Start Time:");
        l5.setBounds(30, 170, 100, 25);
        f.add(l5);

        String[] times = {
                "09:00",
                "10:00",
                "11:00",
                "12:00",
                "14:00",
                "15:00",
                "16:00"
        };

        timeBox = new JComboBox<>(times);
        timeBox.setBounds(150, 170, 200, 25);
        f.add(timeBox);

        JLabel l6 = new JLabel("Duration:");
        l6.setBounds(30, 200, 100, 25);
        f.add(l6);

        durationLabel = new JLabel("1 hour");
        durationLabel.setBounds(150, 200, 200, 25);
        f.add(durationLabel);

        JButton assignBtn = new JButton("Assign Appointment");
        assignBtn.setBounds(80, 240, 150, 30);
        f.add(assignBtn);

        JButton backBtn = new JButton("Back");
        backBtn.setBounds(240, 240, 90, 30);
        f.add(backBtn);

        serviceBox.addActionListener(e -> {

            String service = serviceBox.getSelectedItem().toString();

            if(service.equals("Normal Service")) {

                durationLabel.setText("1 hour");

            } else {

                durationLabel.setText("3 hours");

            }

        });

        assignBtn.addActionListener(e -> {

            String customer = customerBox.getSelectedItem().toString();
            String service = serviceBox.getSelectedItem().toString();
            String technician = technicianBox.getSelectedItem().toString();
            String date = dateField.getText();
            String time = timeBox.getSelectedItem().toString();

            int duration;

            if(service.equals("Normal Service")) {

                duration = 1;

            } else {

                duration = 3;

            }

            String appointmentID = generateAppointmentID();

            saveAppointment(
                    appointmentID,
                    customer,
                    service,
                    technician,
                    date,
                    time,
                    duration
            );

            JOptionPane.showMessageDialog(null,
                    "Appointment Created!");

        });

        backBtn.addActionListener(e -> {

            f.dispose();

            new CounterStaffMenu(user);

        });

        f.setVisible(true);
    }

    private String[] loadCustomers() {

        ArrayList<String> list = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(customerFile))) {

            String line;

            while((line = br.readLine()) != null) {

                String[] data = line.split("\\|");

                if(data[7].equalsIgnoreCase("Customer")
                        && data[8].equalsIgnoreCase("Active")) {

                    list.add(data[0] + " - " + data[1]);

                }

            }

        } catch (Exception e) {}

        return list.toArray(new String[0]);
    }

    private String[] loadTechnicians() {

        ArrayList<String> list = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(technicianFile))) {

            String line;

            while((line = br.readLine()) != null) {

                String[] data = line.split("\\|");

                if(data[7].equalsIgnoreCase("Technician")
                        && data[8].equalsIgnoreCase("Active")) {

                    list.add(data[0] + " - " + data[1]);

                }

            }

        } catch (Exception e) {}

        return list.toArray(new String[0]);
    }

    private String generateAppointmentID() {

        int max = 0;

        File file = new File(appointmentFile);

        try {

            if(!file.exists()) return "A001";

            BufferedReader br = new BufferedReader(new FileReader(file));

            String line;

            while((line = br.readLine()) != null) {

                String[] data = line.split("\\|");

                int num = Integer.parseInt(data[0].substring(1));

                if(num > max)
                    max = num;

            }

        } catch (Exception e) {}

        return String.format("A%03d", max + 1);
    }

    private void saveAppointment(String id,
                                 String customer,
                                 String service,
                                 String technician,
                                 String date,
                                 String time,
                                 int duration) {

        try {

            File folder = new File("data");
            folder.mkdirs();

            BufferedWriter bw =
                    new BufferedWriter(
                            new FileWriter(appointmentFile, true));

            bw.write(id + "|" +
                    customer + "|" +
                    service + "|" +
                    technician + "|" +
                    date + "|" +
                    time + "|" +
                    duration + "|" +
                    "Scheduled");

            bw.newLine();

            bw.close();

        } catch (Exception e) {

            JOptionPane.showMessageDialog(null,
                    "Error saving appointment");

        }
    }
}