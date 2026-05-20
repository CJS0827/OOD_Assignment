package model;

// Custom Checked Exception: thrown when user input fails validation rules.
// Extends Exception (not RuntimeException) so it MUST be handled by the caller.
// Demonstrates Exception Handling OOP concept (Lecture 9.0)
public class InvalidInputException extends Exception {

    // Field-level information for richer error reporting
    private final String fieldName;

    // Constructor with just a message
    public InvalidInputException(String message) {
        super(message);
        this.fieldName = "input";
    }

    // Overloaded constructor with field name for precise error reporting
    public InvalidInputException(String fieldName, String message) {
        super(message);
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }
}