package model;

public class Appointment {

    private String appointmentID;
    private String customerID;
    private String serviceType;
    private String technicianID;
    private String date;
    private String time;
    private int duration;
    private String status;

    public Appointment(String appointmentID,
                       String customerID,
                       String serviceType,
                       String technicianID,
                       String date,
                       String time,
                       int duration,
                       String status) {

        this.appointmentID = appointmentID;
        this.customerID = customerID;
        this.serviceType = serviceType;
        this.technicianID = technicianID;
        this.date = date;
        this.time = time;
        this.duration = duration;
        this.status = status;
    }

    public String getAppointmentID() { return appointmentID; }
    public String getCustomerID() { return customerID; }
    public String getServiceType() { return serviceType; }
    public String getTechnicianID() { return technicianID; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public int getDuration() { return duration; }
    public String getStatus() { return status; }

    public void setStatus(String status) {
        this.status = status;
    }
}