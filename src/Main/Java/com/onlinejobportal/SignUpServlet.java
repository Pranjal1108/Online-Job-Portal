package com.onlinejobportal;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/SignUpServlet")
public class SignUpServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String role = "Job Seeker"; // Default role for new users

        UserDAO1 userDAO = new UserDAO1();

        // Check if the email is already registered
        if (userDAO.isEmailRegistered(email)) {
            response.sendRedirect("Loginpage.html?error=EmailAlreadyExists"); // Redirect with an error
            return;
        }

        // Hash password before saving to the database
        String hashedPassword = PasswordUtil.hashPassword(password);

        // Create a new User object and add to the database
        User newUser = new User(0, username, hashedPassword, email, role, null);
        boolean isAdded = userDAO.addUser(newUser);

        if (isAdded) {
            response.sendRedirect("Loginpage.html?success=SignupComplete"); // Redirect to login page with success
        } else {
            response.sendRedirect("Loginpage.html?error=SignupFailed"); // Redirect with an error
        }
    }
}
