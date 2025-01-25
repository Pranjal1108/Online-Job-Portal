package com.onlinejobportal;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class UserServlet extends HttpServlet {

    private Map<String, String> users = new HashMap<>();

    public UserServlet() {
        users.put("admin@example.com", "adminPassword");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<html><body>");
        out.println("<h1>Welcome to the Online Job Portal!</h1>");
        out.println("<p>This is a test servlet response.</p>");
        out.println("</body></html>");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        if (validateUser(email, password)) {
            HttpSession session = request.getSession();
            session.setAttribute("user", email);

            response.setStatus(HttpServletResponse.SC_OK);
            out.write("{\"status\": \"success\", \"message\": \"Login successful!\"}");
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.write("{\"status\": \"fail\", \"message\": \"Invalid email or password.\"}");
        }

        out.close();
    }

    private boolean validateUser(String email, String password) {
        return users.containsKey(email) && users.get(email).equals(password);
    }
}
