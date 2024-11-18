package pl.kamann.auth.register;

public record RegisterRequest(String email, String password, String firstName, String lastName) {
}