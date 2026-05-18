package model;

// Inheritance: User extends abstract Person (multi-level inheritance hierarchy)
public class User extends Person {

    // Encapsulation: private attributes specific to User
    private String password;
    private String securityQuestion;
    private String securityAnswer;
    private String role;
    private String status;

    public User(String id, String username, String password, String phone,
                String email, String securityQuestion, String securityAnswer, String role, String status) {
        // Call abstract Person superclass constructor
        super(id, username, phone, email);
        this.password = password;
        this.securityQuestion = securityQuestion;
        this.securityAnswer = securityAnswer;
        this.role = role;
        this.status = status;
    }

    public String getPassword() {
        return password;
    }

    public String getSecurityQuestion() {
        return securityQuestion;
    }

    public String getSecurityAnswer() {
        return securityAnswer;
    }

    public String getRole() {
        return role;
    }

    public String getStatus() {
        return status;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setSecurityQuestion(String securityQuestion) {
        this.securityQuestion = securityQuestion;
    }

    public void setSecurityAnswer(String securityAnswer) {
        this.securityAnswer = securityAnswer;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Polymorphism: Implement abstract method from Person
    @Override
    public String getRoleDescription() {
        return "System User with role: " + role;
    }

    // Polymorphism: Implement abstract method from Person
    @Override
    public String getDisplayInfo() {
        return username + " (" + role + ")";
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}