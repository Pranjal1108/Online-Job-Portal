package com.onlinejobportal;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JobServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(JobServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<Job> jobList = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection()) {
            String query = "SELECT * FROM jobs";
            try (PreparedStatement stmt = conn.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Job job = new Job(rs.getInt("id"), rs.getString("title"), rs.getString("description"));
                    jobList.add(job);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error: " + e.getMessage(), e);
            throw new ServletException("Error retrieving job listings", e);
        }

        request.setAttribute("jobList", jobList);
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jobsList.jsp");
        dispatcher.forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int jobId = Integer.parseInt(request.getParameter("jobId"));
        int userId = Integer.parseInt(request.getParameter("userId"));

        try (Connection conn = DBUtil.getConnection()) {
            String query = "INSERT INTO applications (user_id, job_id) VALUES (?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, userId);
                stmt.setInt(2, jobId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error: " + e.getMessage(), e);
            throw new ServletException("Error submitting job application", e);
        }

        response.sendRedirect("jobs");
    }
}

class Job {
    private int id;
    private String title;
    private String description;

    public Job(int id, String title, String description) {
        this.id = id;
        this.title = title;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
