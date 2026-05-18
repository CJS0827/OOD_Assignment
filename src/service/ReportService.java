package service;

import java.io.*;
import java.util.*;

// Abstraction & Modularity: ReportService isolates all analytical
// business logic from the UI layer. Each method returns a single
// well-defined analytical result.
public class ReportService {

    // Encapsulation: private file paths
    private final String APPOINTMENTS_FILE = "data/Appointments.txt";
    private final String PRICES_FILE = "data/prices.txt";
    private final String FEEDBACK_FILE = "data/feedback.txt";
    private final String USERS_FILE = "data/users.txt";

    // === DATA LOADING ===

    // Load all appointments from file
    public ArrayList<String[]> loadAllAppointments() {
        ArrayList<String[]> list = new ArrayList<>();
        File file = new File(APPOINTMENTS_FILE);
        if (!file.exists()) return list;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split("\\|");
                if (data.length >= 10) list.add(data);
            }
        } catch (IOException e) {
            System.out.println("Error loading appointments: " + e.getMessage());
        }
        return list;
    }

    // Load service prices from file → Map<serviceName, price>
    public HashMap<String, Double> loadPrices() {
        HashMap<String, Double> prices = new HashMap<>();
        File file = new File(PRICES_FILE);
        if (!file.exists()) {
            // Default fallback prices
            prices.put("Normal Service", 50.00);
            prices.put("Major Service", 150.00);
            return prices;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split("\\|");
                if (data.length >= 3) {
                    try {
                        prices.put(data[0], Double.parseDouble(data[2]));
                    } catch (NumberFormatException e) {
                        // Skip malformed price entries
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading prices: " + e.getMessage());
        }
        return prices;
    }

    // Resolve a user ID to a username for readable display
    public String resolveUserName(String userId) {
        File file = new File(USERS_FILE);
        if (!file.exists()) return userId;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split("\\|");
                if (data.length >= 2 && data[0].equalsIgnoreCase(userId)) {
                    return data[1];
                }
            }
        } catch (IOException e) { /* fallback to ID */ }
        return userId;
    }

    // === ANALYTICS ===

    // Total revenue from all COMPLETED appointments
    public double calculateTotalRevenue() {
        HashMap<String, Double> prices = loadPrices();
        double total = 0.0;
        for (String[] appt : loadAllAppointments()) {
            if (appt[9].equalsIgnoreCase("Completed")) {
                Double price = prices.get(appt[2]); // service type
                if (price != null) total += price;
            }
        }
        return total;
    }

    // Revenue broken down per service type (for completed appointments only)
    public HashMap<String, Double> calculateRevenuePerService() {
        HashMap<String, Double> prices = loadPrices();
        HashMap<String, Double> revenue = new HashMap<>();
        for (String[] appt : loadAllAppointments()) {
            if (appt[9].equalsIgnoreCase("Completed")) {
                String service = appt[2];
                Double price = prices.get(service);
                if (price != null) {
                    revenue.merge(service, price, Double::sum);
                }
            }
        }
        return revenue;
    }

    // Total count of all appointments
    public int getTotalAppointments() {
        return loadAllAppointments().size();
    }

    // Count of completed appointments
    public int getCompletedCount() {
        int count = 0;
        for (String[] appt : loadAllAppointments()) {
            if (appt[9].equalsIgnoreCase("Completed")) count++;
        }
        return count;
    }

    // Completion rate as a percentage (0-100)
    public double getCompletionRate() {
        int total = getTotalAppointments();
        if (total == 0) return 0.0;
        return (getCompletedCount() * 100.0) / total;
    }

    // Count appointments per service type → Map<serviceName, count>
    public HashMap<String, Integer> getServiceTypeCounts() {
        HashMap<String, Integer> counts = new HashMap<>();
        for (String[] appt : loadAllAppointments()) {
            counts.merge(appt[2], 1, Integer::sum);
        }
        return counts;
    }

    // Most popular service type by count (returns name)
    public String getMostPopularService() {
        HashMap<String, Integer> counts = getServiceTypeCounts();
        String top = "N/A";
        int max = 0;
        for (Map.Entry<String, Integer> e : counts.entrySet()) {
            if (e.getValue() > max) {
                max = e.getValue();
                top = e.getKey();
            }
        }
        return top;
    }

    // === TECHNICIAN ANALYTICS ===

    // For each technician (by ID), how many appointments they've handled
    public HashMap<String, Integer> getAppointmentsPerTechnician() {
        HashMap<String, Integer> map = new HashMap<>();
        for (String[] appt : loadAllAppointments()) {
            map.merge(appt[3], 1, Integer::sum);
        }
        return map;
    }

    // For each technician (by ID), how many COMPLETED appointments
    public HashMap<String, Integer> getCompletedPerTechnician() {
        HashMap<String, Integer> map = new HashMap<>();
        for (String[] appt : loadAllAppointments()) {
            if (appt[9].equalsIgnoreCase("Completed")) {
                map.merge(appt[3], 1, Integer::sum);
            }
        }
        return map;
    }

    // Average rating per technician (read from feedback.txt).
    // feedback.txt format: appointmentID|fromName|rating|comment|date
    // We join via appointments.txt to find the technician for each feedback row.
    public HashMap<String, Double> getAverageRatingPerTechnician() {
        HashMap<String, ArrayList<Integer>> ratings = new HashMap<>();
        ArrayList<String[]> appts = loadAllAppointments();

        File file = new File(FEEDBACK_FILE);
        if (!file.exists()) return new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] fb = line.split("\\|");
                if (fb.length < 5) continue;
                String apptID = fb[0];
                int rating;
                try {
                    rating = Integer.parseInt(fb[2]);
                } catch (NumberFormatException e) {
                    continue;
                }
                // Find technician for this appointment
                for (String[] appt : appts) {
                    if (appt[0].equalsIgnoreCase(apptID)) {
                        ratings.computeIfAbsent(appt[3], k -> new ArrayList<>()).add(rating);
                        break;
                    }
                }
            }
        } catch (IOException e) { /* return empty map */ }

        // Compute averages
        HashMap<String, Double> averages = new HashMap<>();
        for (Map.Entry<String, ArrayList<Integer>> e : ratings.entrySet()) {
            double sum = 0;
            for (int r : e.getValue()) sum += r;
            averages.put(e.getKey(), sum / e.getValue().size());
        }
        return averages;
    }

    // Best technician by completed appointments count
    public String getTopTechnician() {
        HashMap<String, Integer> map = getCompletedPerTechnician();
        String top = "N/A";
        int max = 0;
        for (Map.Entry<String, Integer> e : map.entrySet()) {
            if (e.getValue() > max) {
                max = e.getValue();
                top = e.getKey();
            }
        }
        return top.equals("N/A") ? "N/A" : resolveUserName(top);
    }
}