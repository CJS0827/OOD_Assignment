package ui;

import model.User;
import service.AppointmentService;

import javax.swing.*;
import java.awt.Color;

public class CreateAppointment {

    private JComboBox<String> customerBox;
    private JComboBox<String> serviceBox;
    private JComboBox<String> technicianBox;
    private JComboBox<String> timeBox;

    private JTextField dateField;
    private JTextField plateField;
    private JTextField modelField;

    private JLabel durationLabel;

    private AppointmentService service = new AppointmentService();

    public CreateAppointment(User user) {

        JFrame f = new JFrame("Create Appointment");
        f.setSize(420, 450);
        f.setLayout(null);
        f.setLocationRelativeTo(null);

        // title
        JLabel title = new JLabel("Create Appointment");
        title.setBounds(140, 10, 200, 25);
        f.add(title);

        // customer
        JLabel l1 = new JLabel("Customer:");
        l1.setBounds(30, 50, 100, 25);
        f.add(l1);

        customerBox = new JComboBox<>(service.loadCustomers());
        customerBox.setBounds(150, 50, 200, 25);
        f.add(customerBox);

        // service type
        JLabel l2 = new JLabel("Service Type:");
        l2.setBounds(30, 80, 100, 25);
        f.add(l2);

        String[] services = {"Normal Service", "Major Service"};
        serviceBox = new JComboBox<>(services);
        serviceBox.setBounds(150, 80, 200, 25);
        f.add(serviceBox);

        // technician
        JLabel l3 = new JLabel("Technician:");
        l3.setBounds(30, 110, 100, 25);
        f.add(l3);

        technicianBox = new JComboBox<>(service.loadTechnicians());
        technicianBox.setBounds(150, 110, 200, 25);
        f.add(technicianBox);

        // date
        JLabel l4 = new JLabel("Date:");
        l4.setBounds(30, 140, 100, 25);
        f.add(l4);

        dateField = new JTextField("2026-04-10");
        dateField.setBounds(150, 140, 200, 25);
        f.add(dateField);

        // time
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

        // duration label
        JLabel l6 = new JLabel("Duration:");
        l6.setBounds(30, 200, 100, 25);
        f.add(l6);

        durationLabel = new JLabel("1 hour");
        durationLabel.setBounds(150, 200, 200, 25);
        f.add(durationLabel);

        // plate
        JLabel l7 = new JLabel("Car Plate:");
        l7.setBounds(30, 230, 100, 25);
        f.add(l7);

        plateField = new JTextField();
        plateField.setBounds(150, 230, 200, 25);
        f.add(plateField);

        // model
        JLabel l8 = new JLabel("Vehicle Model:");
        l8.setBounds(30, 260, 100, 25);
        f.add(l8);

        modelField = new JTextField();
        modelField.setBounds(150, 260, 200, 25);
        f.add(modelField);

        // buttons
        JButton assignBtn = new JButton("Assign Appointment");
        assignBtn.setBounds(130, 310, 150, 30);
        f.add(assignBtn);

        JButton backBtn = new JButton("< Back");
        backBtn.setBounds(10, 10, 80, 25);
        f.add(backBtn);


        // change duration automatically
        serviceBox.addActionListener(e -> {

            String selected = serviceBox.getSelectedItem().toString();

            if(selected.equals("Normal Service"))
                durationLabel.setText("1 hour");
            else
                durationLabel.setText("3 hours");

        });


        // assign button click
        assignBtn.addActionListener(e -> {

            String customer =
                    (customerBox.getSelectedItem() != null)
                            ? customerBox.getSelectedItem().toString()
                            : "";

            String serviceType =
                    (serviceBox.getSelectedItem() != null)
                            ? serviceBox.getSelectedItem().toString()
                            : "";

            String technician =
                    (technicianBox.getSelectedItem() != null)
                            ? technicianBox.getSelectedItem().toString()
                            : "";

            String date = dateField.getText().trim();
            String time = timeBox.getSelectedItem().toString();
            String plate = plateField.getText().trim();
            String model = modelField.getText().trim();

            boolean valid = true;


            // reset borders
            customerBox.setBorder(UIManager.getBorder("ComboBox.border"));
            serviceBox.setBorder(UIManager.getBorder("ComboBox.border"));
            technicianBox.setBorder(UIManager.getBorder("ComboBox.border"));
            dateField.setBorder(UIManager.getBorder("TextField.border"));
            plateField.setBorder(UIManager.getBorder("TextField.border"));
            modelField.setBorder(UIManager.getBorder("TextField.border"));

            // validation
            if(customer.isEmpty()){
                customerBox.setBorder(BorderFactory.createLineBorder(Color.RED));
                valid = false;
            }

            if(serviceType.isEmpty()){
                serviceBox.setBorder(BorderFactory.createLineBorder(Color.RED));
                valid = false;
            }

            if(technician.isEmpty()){
                technicianBox.setBorder(BorderFactory.createLineBorder(Color.RED));
                valid = false;
            }

            if(date.isEmpty()){
                dateField.setBorder(BorderFactory.createLineBorder(Color.RED));
                valid = false;
            }

            if(plate.isEmpty()){
                plateField.setBorder(BorderFactory.createLineBorder(Color.RED));
                valid = false;
            }

            if(model.isEmpty()){
                modelField.setBorder(BorderFactory.createLineBorder(Color.RED));
                valid = false;
            }

            if(!valid){
                JOptionPane.showMessageDialog(
                        f,
                        "Please fill in all required fields"
                );
                return;
            }

            int duration =
                    serviceType.equals("Normal Service")
                            ? 1
                            : 3;

            String appointmentID =
                    service.generateAppointmentID();

            service.saveAppointment(

                    appointmentID,
                    customer,
                    serviceType,
                    technician,
                    date,
                    time,
                    duration,
                    plate,
                    model
            );


            JOptionPane.showMessageDialog(
                    f,
                    "Appointment Created Successfully"
            );

            f.dispose();
            new CounterStaffMenu(user);

        });

        backBtn.addActionListener(e -> {

            f.dispose();

            new CounterStaffMenu(user);

        });

        f.setVisible(true);

    }

}