package service;

import java.io.*;
import java.util.ArrayList;

public class AppointmentService {

    private final String userFile        = "data/users.txt";
    private final String appointmentFile = "data/Appointments.txt";

    // Returns "U005|Elephant", "U006|Darren", etc. for Customer role
    public String[] loadCustomers() {
        return loadUsersByRole("Customer");
    }

    // Returns "U003|Cat", etc. for Technician role
    public String[] loadTechnicians() {
        return loadUsersByRole("Technician");
    }

    private String[] loadUsersByRole(String role) {
        ArrayList<String> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(userFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split("\\|");
                if (data.length >= 9
                        && data[7].equalsIgnoreCase(role)
                        && data[8].equalsIgnoreCase("Active")) {
                    // Store "ID|Username" so UI can split and display name
                    list.add(data[0] + "|" + data[1]);
                }
            }
        } catch (Exception e) {}
        return list.toArray(new String[0]);
    }

    // Resolve a user ID to a username (for display purposes)
    public String getUsernameById(String userId) {
        try (BufferedReader br = new BufferedReader(new FileReader(userFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split("\\|");
                if (data.length >= 2 && data[0].equalsIgnoreCase(userId)) {
                    return data[1];
                }
            }
        } catch (Exception e) {}
        return userId; // fallback: return ID if not found
    }

    public String generateAppointmentID() {
        int max = 0;
        File file = new File(appointmentFile);
        if (!file.exists()) return "A001";
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split("\\|");
                if (data.length >= 1) {
                    int num = Integer.parseInt(data[0].substring(1));
                    if (num > max) max = num;
                }
            }
        } catch (Exception e) {}
        return String.format("A%03d", max + 1);
    }

    // Saves appointment using user IDs (not usernames)
    public void saveAppointment(
            String id,
            String customerID,
            String service,
            String technicianID,
            String date,
            String time,
            int duration,
            String plate,
            String model
    ) {
        try {
            new File("data").mkdirs();
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(appointmentFile, true))) {
                bw.write(id + "|" +
                        customerID + "|" +
                        service + "|" +
                        technicianID + "|" +
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

    // Load all appointments as raw string arrays for display
    public ArrayList<String[]> loadAllAppointments() {
        ArrayList<String[]> list = new ArrayList<>();
        File file = new File(appointmentFile);
        if (!file.exists()) return list;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split("\\|");
                if (data.length >= 10) list.add(data);
            }
        } catch (Exception e) {}
        return list;
    }

    // Load appointments for a specific user ID (customer or technician)
    public ArrayList<String[]> loadAppointmentsByUser(String userID) {
        ArrayList<String[]> list = new ArrayList<>();
        for (String[] appt : loadAllAppointments()) {
            // appt[1] = customerID, appt[3] = technicianID
            if (appt[1].equalsIgnoreCase(userID) || appt[3].equalsIgnoreCase(userID)) {
                list.add(appt);
            }
        }
        return list;
    }

    // Update appointment status by appointment ID
    public boolean updateAppointmentStatus(String appointmentID, String newStatus) {
        File file = new File(appointmentFile);
        ArrayList<String> lines = new ArrayList<>();
        boolean found = false;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split("\\|");
                if (data.length >= 10 && data[0].equalsIgnoreCase(appointmentID)) {
                    data[9] = newStatus;
                    lines.add(String.join("|", data));
                    found = true;
                } else {
                    lines.add(line);
                }
            }
        } catch (Exception e) { return false; }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            for (String l : lines) {
                bw.write(l);
                bw.newLine();
            }
        } catch (Exception e) { return false; }
        return found;
    }
}