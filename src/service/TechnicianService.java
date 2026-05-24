package service;

import java.io.*;
import java.util.ArrayList;

/**
 * TechnicianService handles all file I/O for the Technician module.
 *
 * OOP Concepts:
 * - Encapsulation: all file paths are private constants; UI classes never
 *   read or write files directly — they call public methods here only.
 * - Abstraction: callers ask for data ("get my appointments") without
 *   knowing anything about how or where it is stored.
 *
 * Files used:
 *   data/Appointments.txt  ID|CustomerID|ServiceType|TechnicianID|Date|Time|Duration|Plate|Model|Status|CounterStaffID
 *   data/Feedbacks.txt     FeedbackID|AppointmentID|TechnicianID|Comment|Date
 *   data/Comments.txt      CommentID|AppointmentID|CustomerID|TargetID|TargetRole|Comment|Date
 *   data/users.txt         ID|Username|Password|Phone|Email|SecQ|SecA|Role|Status
 */
public class TechnicianService {

    private static final String APPOINTMENT_FILE = "data/Appointments.txt";
    private static final String FEEDBACK_FILE    = "data/Feedbacks.txt";
    private static final String COMMENT_FILE     = "data/Comments.txt";
    private static final String USER_FILE        = "data/users.txt";

    // ── APPOINTMENTS ─────────────────────────────────────────────────────────

    /** Return every appointment whose TechnicianID matches the given ID. */
    public ArrayList<String[]> getAppointmentsByTechnician(String technicianID) {
        ArrayList<String[]> result = new ArrayList<>();
        File file = new File(APPOINTMENT_FILE);
        if (!file.exists()) return result;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] d = line.split("\\|");
                if (d.length >= 10 && d[3].equalsIgnoreCase(technicianID))
                    result.add(d);
            }
        } catch (IOException e) {
            System.out.println("Error reading appointments: " + e.getMessage());
        }
        return result;
    }

    /**
     * Set a specific appointment's Status field to "Completed".
     * @return true if the record was found and saved.
     */
    public boolean markAppointmentCompleted(String appointmentID) {
        File file = new File(APPOINTMENT_FILE);
        ArrayList<String> lines = new ArrayList<>();
        boolean found = false;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] d = line.split("\\|");
                if (d.length >= 10 && d[0].equalsIgnoreCase(appointmentID)) {
                    d[9] = "Completed";
                    lines.add(String.join("|", d));
                    found = true;
                } else {
                    lines.add(line);
                }
            }
        } catch (IOException e) { return false; }
        if (!found) return false;
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            for (String l : lines) { bw.write(l); bw.newLine(); }
        } catch (IOException e) { return false; }
        return true;
    }

    // ── FEEDBACKS ────────────────────────────────────────────────────────────

    /** True if the technician has already submitted feedback for this appointment. */
    public boolean hasFeedback(String appointmentID, String technicianID) {
        File file = new File(FEEDBACK_FILE);
        if (!file.exists()) return false;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] d = line.split("\\|");
                if (d.length >= 3 && d[1].equalsIgnoreCase(appointmentID)
                        && d[2].equalsIgnoreCase(technicianID))
                    return true;
            }
        } catch (IOException e) {
            System.out.println("Error reading feedbacks: " + e.getMessage());
        }
        return false;
    }

    /** Append a new feedback row to Feedbacks.txt. */
    public boolean saveFeedback(String appointmentID, String technicianID,
                                String comment, String date) {
        new File("data").mkdirs();
        File file = new File(FEEDBACK_FILE);
        String id = nextFeedbackID(file);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {
            bw.write(id + "|" + appointmentID + "|" + technicianID + "|" + comment + "|" + date);
            bw.newLine();
            return true;
        } catch (IOException e) {
            System.out.println("Error saving feedback: " + e.getMessage());
            return false;
        }
    }

    /** All feedbacks written by this technician. */
    public ArrayList<String[]> getFeedbacksByTechnician(String technicianID) {
        ArrayList<String[]> result = new ArrayList<>();
        File file = new File(FEEDBACK_FILE);
        if (!file.exists()) return result;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] d = line.split("\\|");
                if (d.length >= 5 && d[2].equalsIgnoreCase(technicianID))
                    result.add(d);
            }
        } catch (IOException e) {
            System.out.println("Error reading feedbacks: " + e.getMessage());
        }
        return result;
    }

    private String nextFeedbackID(File file) {
        int max = 0;
        if (!file.exists()) return "F001";
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] d = line.split("\\|");
                if (d.length >= 1 && d[0].startsWith("F")) {
                    try { int n = Integer.parseInt(d[0].substring(1)); if (n > max) max = n; }
                    catch (NumberFormatException ignored) {}
                }
            }
        } catch (IOException e) {
            System.out.println("Error generating feedback ID: " + e.getMessage());
        }
        return String.format("F%03d", max + 1);
    }

    // ── COMMENTS ─────────────────────────────────────────────────────────────

    /** All customer comments where TargetID = technicianID and TargetRole = "Technician". */
    public ArrayList<String[]> getCommentsForTechnician(String technicianID) {
        ArrayList<String[]> result = new ArrayList<>();
        File file = new File(COMMENT_FILE);
        if (!file.exists()) return result;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] d = line.split("\\|");
                if (d.length >= 7 && d[3].equalsIgnoreCase(technicianID)
                        && d[4].equalsIgnoreCase("Technician"))
                    result.add(d);
            }
        } catch (IOException e) {
            System.out.println("Error reading comments: " + e.getMessage());
        }
        return result;
    }

    // ── USERS ────────────────────────────────────────────────────────────────

    /** Resolve a user ID like "U005" to the stored username, e.g. "Elephant". */
    public String getUsernameById(String userID) {
        File file = new File(USER_FILE);
        if (!file.exists()) return userID;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] d = line.split("\\|");
                if (d.length >= 2 && d[0].equalsIgnoreCase(userID)) return d[1];
            }
        } catch (IOException e) {
            System.out.println("Error reading users: " + e.getMessage());
        }
        return userID;
    }

    /**
     * Overwrite the editable fields for one user in users.txt.
     * Username, role and status are preserved.
     */
    public boolean updateProfile(String userId, String phone, String email,
                                 String password, String secQ, String secA) {
        File file = new File(USER_FILE);
        ArrayList<String> lines = new ArrayList<>();
        boolean found = false;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] u = line.split("\\|");
                if (u.length >= 9 && u[0].equals(userId)) {
                    lines.add(u[0]+"|"+u[1]+"|"+password+"|"+phone+"|"+email+"|"+secQ+"|"+secA+"|"+u[7]+"|"+u[8]);
                    found = true;
                } else {
                    lines.add(line);
                }
            }
        } catch (IOException e) { return false; }
        if (!found) return false;
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            for (String l : lines) { bw.write(l); bw.newLine(); }
        } catch (IOException e) { return false; }
        return true;
    }
}
