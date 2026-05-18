package service;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * PaymentService handles all file I/O for the payment feature.
 *
 * OOP Concepts:
 * - Encapsulation: all file paths are private; data is accessed only through public methods
 * - Single Responsibility: this class only manages payment and price data
 *
 * Files managed:
 *   data/Prices.txt       - Format: ServiceType|Price
 *   data/Payments.txt     - Format: PaymentID|AppointmentID|CustomerID|ServiceType|Amount|Date|Method
 *   data/Appointments.txt - Format: ID|CustomerID|ServiceType|TechnicianID|Date|Time|Duration|Plate|Model|Status
 *   data/users.txt        - Format: ID|Username|Password|Phone|Email|SecQ|SecA|Role|Status
 */
public class PaymentService {

    private final String priceFile       = "data/Prices.txt";
    private final String paymentFile     = "data/Payments.txt";
    private final String appointmentFile = "data/Appointments.txt";
    private final String userFile        = "data/users.txt";

    // ─────────────────────────────────────────────
    //  PRICES
    // ─────────────────────────────────────────────

    /**
     * Load service prices from Prices.txt.
     * Returns a map: "Normal Service" -> 50.00, "Major Service" -> 150.00
     * Falls back to hardcoded defaults if file is missing (so nothing breaks
     * while Manager's Set Price feature is not yet built).
     */
    public HashMap<String, Double> loadPrices() {
        HashMap<String, Double> prices = new HashMap<>();

        // Default fallback prices
        prices.put("Normal Service", 50.00);
        prices.put("Major Service", 150.00);

        File file = new File(priceFile);
        if (!file.exists()) return prices; // return defaults

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split("\\|");
                if (data.length >= 2) {
                    try {
                        prices.put(data[0].trim(), Double.parseDouble(data[1].trim()));
                    } catch (NumberFormatException ignored) {}
                }
            }
        } catch (IOException e) {
            // return defaults already set above
        }

        return prices;
    }

    /**
     * Get price for a specific service type.
     */
    public double getPrice(String serviceType) {
        HashMap<String, Double> prices = loadPrices();
        return prices.getOrDefault(serviceType, 0.00);
    }

    // ─────────────────────────────────────────────
    //  APPOINTMENTS
    // ─────────────────────────────────────────────

    /**
     * Load all appointments that are "Completed" and not yet paid.
     * (We check Payments.txt to exclude already-paid appointments.)
     * Returns list of: [ID, CustomerID, ServiceType, TechnicianID, Date, Time, Duration, Plate, Model, Status]
     */
    public ArrayList<String[]> loadUnpaidCompletedAppointments() {
        ArrayList<String> paidApptIDs = getPaidAppointmentIDs();
        ArrayList<String[]> result = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(appointmentFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split("\\|");
                if (data.length >= 10
                        && data[9].equalsIgnoreCase("Completed")
                        && !paidApptIDs.contains(data[0])) {
                    result.add(data);
                }
            }
        } catch (IOException e) {}

        return result;
    }

    /**
     * Load ALL appointments (Scheduled + Completed) that haven't been paid.
     * Counter staff may need to collect payment for scheduled ones too.
     */
    public ArrayList<String[]> loadAllUnpaidAppointments() {
        ArrayList<String> paidApptIDs = getPaidAppointmentIDs();
        ArrayList<String[]> result = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(appointmentFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split("\\|");
                if (data.length >= 10 && !paidApptIDs.contains(data[0])) {
                    result.add(data);
                }
            }
        } catch (IOException e) {}

        return result;
    }

    // ─────────────────────────────────────────────
    //  PAYMENTS
    // ─────────────────────────────────────────────

    /**
     * Returns a list of appointment IDs that already have a payment recorded.
     */
    private ArrayList<String> getPaidAppointmentIDs() {
        ArrayList<String> paidIDs = new ArrayList<>();
        File file = new File(paymentFile);
        if (!file.exists()) return paidIDs;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split("\\|");
                if (data.length >= 2) paidIDs.add(data[1]); // index 1 = AppointmentID
            }
        } catch (IOException e) {}

        return paidIDs;
    }

    /**
     * Save a new payment record to Payments.txt.
     * Format: PaymentID|AppointmentID|CustomerID|ServiceType|Amount|Date|Method
     * Returns the generated PaymentID, or null if failed.
     */
    public String savePayment(String appointmentID, String customerID,
                              String serviceType, double amount, String method) {
        try {
            new File("data").mkdirs();
            String paymentID = generatePaymentID();
            String date = new java.text.SimpleDateFormat("yyyy-MM-dd")
                              .format(new java.util.Date());

            try (BufferedWriter bw = new BufferedWriter(new FileWriter(paymentFile, true))) {
                bw.write(paymentID + "|" +
                         appointmentID + "|" +
                         customerID + "|" +
                         serviceType + "|" +
                         String.format("%.2f", amount) + "|" +
                         date + "|" +
                         method);
                bw.newLine();
            }
            return paymentID;
        } catch (IOException e) {
            return null;
        }
    }

    private String generatePaymentID() {
        int max = 0;
        File file = new File(paymentFile);
        if (!file.exists()) return "P001";
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split("\\|");
                if (data.length >= 1 && data[0].startsWith("P")) {
                    int num = Integer.parseInt(data[0].substring(1));
                    if (num > max) max = num;
                }
            }
        } catch (Exception e) {}
        return String.format("P%03d", max + 1);
    }

    // ─────────────────────────────────────────────
    //  USER LOOKUP
    // ─────────────────────────────────────────────

    /**
     * Resolve a user ID to a display username.
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
        return userID; // fallback: return ID if not found
    }

    // ─────────────────────────────────────────────
    //  LOAD ALL PAYMENTS (for Payment Records screen)
    // ─────────────────────────────────────────────

    /**
     * Load every payment record from Payments.txt.
     * Each element: [PaymentID, AppointmentID, CustomerID, ServiceType, Amount, Date, Method]
     */
    public ArrayList<String[]> loadAllPayments() {
        ArrayList<String[]> result = new ArrayList<>();
        File file = new File(paymentFile);
        if (!file.exists()) return result;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split("\\|");
                if (data.length >= 7) result.add(data);
            }
        } catch (IOException e) {}
        return result;
    }

    /**
     * Load every appointment record as raw String arrays.
     * Each element: [ID, CustomerID, ServiceType, TechnicianID, Date, Time, Duration, Plate, Model, Status, ...]
     */
    public ArrayList<String[]> loadAllAppointmentsRaw() {
        ArrayList<String[]> result = new ArrayList<>();
        File file = new File(appointmentFile);
        if (!file.exists()) return result;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split("\\|");
                if (data.length >= 10) result.add(data);
            }
        } catch (IOException e) {}
        return result;
    }

}