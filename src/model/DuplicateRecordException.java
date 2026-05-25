package model;

// Custom Checked Exception: thrown when trying to create a record
// that violates uniqueness rules (e.g., duplicate username or email).
// Demonstrates Exception Handling OOP concept (Lecture 9.0)
public class DuplicateRecordException extends Exception {

    public DuplicateRecordException(String message) {
        super(message);
    }
}