package pl.kamann.auth.register;

import java.util.Set;

public record RegisterRequest(String email, String password, String firstName, String lastName) {
}