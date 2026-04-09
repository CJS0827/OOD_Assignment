package service;

import java.io.*;
import java.util.ArrayList;

public class AppointmentService {

    private final String customerFile = "data/users.txt";
    private final String technicianFile = "data/users.txt";
    private final String appointmentFile = "data/appointments.txt";

    public String[] loadCustomers() {

        ArrayList<String> list = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(customerFile))) {

            String line;

            while((line = br.readLine()) != null) {

                String[] data = line.split("\\|");

                if(data[7].equalsIgnoreCase("Customer")
                        && data[8].equalsIgnoreCase("Active")) {

                    list.add(data[1]);
                }
            }

        } catch (Exception e) {}

        return list.toArray(new String[0]);
    }


    public String[] loadTechnicians() {

        ArrayList<String> list = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(technicianFile))) {

            String line;

            while((line = br.readLine()) != null) {

                String[] data = line.split("\\|");

                if(data[7].equalsIgnoreCase("Technician")
                        && data[8].equalsIgnoreCase("Active")) {

                    list.add(data[1]);
                }
            }

        } catch (Exception e) {}

        return list.toArray(new String[0]);
    }


    public String generateAppointmentID() {

        int max = 0;

        File file = new File(appointmentFile);

        if (!file.exists()) return "A001";

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {

            String line;

            while ((line = br.readLine()) != null) {

                String[] data = line.split("\\|");

                int num = Integer.parseInt(data[0].substring(1));

                if (num > max)
                    max = num;

            }

        } catch (Exception e) {}

        return String.format("A%03d", max + 1);
    }



    public void saveAppointment(
            String id,
            String customer,
            String service,
            String technician,
            String date,
            String time,
            int duration,
            String plate,
            String model
    ) {

        try {

            File folder = new File("data");
            folder.mkdirs();

            try (BufferedWriter bw =
                         new BufferedWriter(
                                 new FileWriter(appointmentFile, true))) {

                bw.write(id + "|" +
                        customer + "|" +
                        service + "|" +
                        technician + "|" +
                        date + "|" +
                        time + "|" +
                        duration + "|" +
                        plate + "|" +
                        model + "|" +
                        "Scheduled");

                bw.newLine();
            }

        } catch (Exception e) {

            System.out.println("Error saving appointment");

        }
    }

}