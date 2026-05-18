package service;

import java.io.*;
import java.util.ArrayList;

/**
 * CustomerService handles all file I/O for the Customer module.
 *
 * OOP Concepts used:
 * - Encapsulation: all file paths are private; data access is through public methods only
 * - Single Responsibility: this class only handles customer-related data operations
 *
 * Files managed:
 *   data/Appointments.txt  - Format: ID|CustomerID|ServiceType|TechnicianID|Date|Time|Duration|Plate|Model|Status
 *   data/Payments.txt      - Format: PaymentID|AppointmentID|CustomerID|ServiceType|Amount|Date|Method
 *   data/Feedbacks.txt     - Format: FeedbackID|AppointmentID|TechnicianID|Comment|Date
 *   data/Comments.txt      - Format: CommentID|AppointmentID|CustomerID|TargetID|TargetRole|Comment|Date
 *   data/users.txt         - Format: ID|Username|Password|Phone|Email|SecQ|SecA|Role|Status
 */
public class CustomerService {

    private final String appointmentFile = "data/Appointments.txt";
    private final String paymentFile     = "data/Payments.txt";
    private final String feedbackFile    = "data/Feedbacks.txt";
    private final String commentFile     = "data/Comments.txt";
    private final String userFile        = "data/users.txt";

    // ─────────────────────────────────────────────
    //  APPOINTMENTS
    // ─────────────────────────────────────────────

    /**
     * Load all appointments belonging to a specific customer.
     * Each element: [ID, CustomerID, ServiceType, TechnicianID, Date, Time, Duration, Plate, Model, Status]
     */
    public ArrayList<String[]> getAppointmentsByCustomer(String customerID) {
        ArrayList<String[]> result = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(appointmentFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split("\\|");
                if (data.length >= 10 && data[1].equalsIgnoreCase(customerID)) {
                    result.add(data);
                }
            }
        } catch (IOException e) {
            // file may not exist yet — return empty list
        }
        return result;
    }

    // ─────────────────────────────────────────────
    //  PAYMENTS
    // ─────────────────────────────────────────────

    /**
     * Load all payment records for a specific customer.
     * Each element: [PaymentID, AppointmentID, CustomerID, ServiceType, Amount, Date, Method]
     */
    public ArrayList<String[]> getPaymentsByCustomer(String customerID) {
        ArrayList<String[]> result = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(paymentFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split("\\|");
                if (data.length >= 7 && data[2].equalsIgnoreCase(customerID)) {
                    result.add(data);
                }
            }
        } catch (IOException e) {
            // file may not exist yet
        }
        return result;
    }

    // ─────────────────────────────────────────────
    //  FEEDBACKS (written by technicians)
    // ─────────────────────────────────────────────

    /**
     * Load all technician feedbacks for a customer's appointments.
     * Each element: [FeedbackID, AppointmentID, TechnicianID, Comment, Date]
     */
    public ArrayList<String[]> getFeedbacksByCustomer(String customerID) {
        // First collect all appointment IDs belonging to this customer
        ArrayList<String> myAppointmentIDs = new ArrayList<>();
        for (String[] appt : getAppointmentsByCustomer(customerID)) {
            myAppointmentIDs.add(appt[0]);
        }

        ArrayList<String[]> result = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(feedbackFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split("\\|");
                if (data.length >= 5 && myAppointmentIDs.contains(data[1])) {
                    result.add(data);
                }
            }
        } catch (IOException e) {
            // file may not exist yet
        }
        return result;
    }

    // ─────────────────────────────────────────────
    //  COMMENTS (written by customers)
    // ─────────────────────────────────────────────

    /**
     * Load all comments already written by a customer.
     * Each element: [CommentID, AppointmentID, CustomerID, TargetID, TargetRole, Comment, Date]
     */
    public ArrayList<String[]> getCommentsByCustomer(String customerID) {
        ArrayList<String[]> result = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(commentFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split("\\|");
                if (data.length >= 7 && data[2].equalsIgnoreCase(customerID)) {
                    result.add(data);
                }
            }
        } catch (IOException e) {
            // file may not exist yet
        }
        return result;
    }

    /**
     * Check whether the customer already commented on a specific appointment for a given target (staff/tech).
     */
    public boolean hasCommented(String appointmentID, String customerID, String targetID) {
        for (String[] c : getCommentsByCustomer(customerID)) {
            if (c[1].equalsIgnoreCase(appointmentID) && c[3].equalsIgnoreCase(targetID)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Save a new comment to Comments.txt.
     * Format: CommentID|AppointmentID|CustomerID|TargetID|TargetRole|Comment|Date
     */
    public boolean saveComment(String appointmentID, String customerID,
                               String targetID, String targetRole, String comment) {
        try {
            new File("data").mkdirs();
            String id = generateCommentID();
            String date = new java.text.SimpleDateFormat("yyyy-MM-dd")
                              .format(new java.util.Date());

            try (BufferedWriter bw = new BufferedWriter(new FileWriter(commentFile, true))) {
                bw.write(id + "|" + appointmentID + "|" + customerID + "|" +
                         targetID + "|" + targetRole + "|" + comment + "|" + date);
                bw.newLine();
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private String generateCommentID() {
        int max = 0;
        File file = new File(commentFile);
        if (!file.exists()) return "C001";
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split("\\|");
                if (data.length >= 1 && data[0].startsWith("C")) {
                    int num = Integer.parseInt(data[0].substring(1));
                    if (num > max) max = num;
                }
            }
        } catch (Exception e) {}
        return String.format("C%03d", max + 1);
    }

    // ─────────────────────────────────────────────
    //  USERS (lookup helpers)
    // ─────────────────────────────────────────────

    /**
     * Resolve a user ID to a display name.
     */
    public String getUsernameById(String userID) {
        try (BufferedReader br = new BufferedReader(new FileReader(userFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split("\\|");
                if (data.length >= 2 && data[0].equalsIgnoreCase(userID)) {
                    return data[1];
                }
            }
        } catch (IOException e) {}
        return userID; // fallback
    }

    /**
     * Get the counter staff ID assigned to an appointment.
     * Since counter staff create appointments, we look up who created it
     * by checking the Appointments file. If not stored, return a default.
     *
     * NOTE: Your Appointments.txt doesn't currently store counter staff ID.
     * This helper returns all active counter staff so the customer can comment on them.
     */
    public ArrayList<String[]> getCounterStaffForAppointment(String appointmentID) {
        // Returns all active CounterStaff as [ID, Username]
        ArrayList<String[]> result = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(userFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split("\\|");
                if (data.length >= 9
                        && data[7].equalsIgnoreCase("CounterStaff")
                        && data[8].equalsIgnoreCase("Active")) {
                    result.add(new String[]{data[0], data[1]});
                }
            }
        } catch (IOException e) {}
        return result;
    }
}
