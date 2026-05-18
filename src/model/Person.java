package model;

// Abstraction: Person is an abstract base class that defines common attributes
// and behavior for all people in the system (Users, Managers, etc.)
// Cannot be instantiated directly — must be extended by concrete subclasses.
public abstract class Person {

    // Encapsulation: protected attributes accessible to subclasses
    protected String id;
    protected String username;
    protected String phone;
    protected String email;

    // Constructor for subclasses to call via super()
    public Person(String id, String username, String phone, String email) {
        this.id = id;
        this.username = username;
        this.phone = phone;
        this.email = email;
    }

    // Concrete getters — shared by all subclasses
    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    // Concrete setters — shared by all subclasses
    public void setUsername(String username) {
        this.username = username;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // Abstract method: each subclass MUST provide its own role description.
    // This forces subclasses to define their identity in the system.
    public abstract String getRoleDescription();

    // Abstract method: each subclass MUST define how it represents itself.
    public abstract String getDisplayInfo();
}