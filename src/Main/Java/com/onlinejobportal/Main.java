package com.onlinejobportal;

public class Main {
    public static void main(String[] args) {
        UserDAO1 userDAO = new UserDAO1();

        
        User newUser = new User(0, "testuser", "Pranjal08@", "testuser@example.com", "Job Seeker", null);
        boolean added = userDAO.addUser(newUser);

        if (added) {
            System.out.println("User added successfully!");
        } else {
            System.out.println("Failed to add user.");
        }
    }
}
