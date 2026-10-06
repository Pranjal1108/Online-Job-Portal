package com.jobify;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
@Component
public class SeedData implements CommandLineRunner {
    private final JdbcTemplate db;
    public SeedData(JdbcTemplate db) { this.db = db; }
    @Override @Transactional public void run(String... args) {
        if (db.queryForObject("SELECT COUNT(*) FROM jobs", Integer.class) != 0) return;
        String[][] rows = {
            {"Frontend Engineer", "Forma Studio", "Bengaluru", "Engineering", "Full-time", "Hybrid", "1200000", "2000000", "React,TypeScript,Accessibility", "Build thoughtful interfaces for a collaborative design platform. Work with designers to ship accessible, fast experiences. You will own reusable React components, improve performance, and participate in code reviews.\n\nWhat you bring\nStrong JavaScript fundamentals, experience with React and TypeScript, and attention to interaction design. A portfolio of working projects matters more than a specific degree."},
            {"Product Designer", "Monograph", "Mumbai", "Design", "Full-time", "Remote", "1000000", "1800000", "Figma,Research,Prototyping", "Shape a product used by small creative teams. Lead discovery, map user journeys, and prototype solutions with engineering.\n\nWhat you bring\nA portfolio showing your process, comfort with user research, and clear communication. Experience with design systems is a plus."},
            {"Backend Developer", "Fieldwork Labs", "Hyderabad", "Engineering", "Full-time", "Hybrid", "1400000", "2400000", "Java,Spring Boot,SQL", "Design reliable APIs and data models for a logistics product. Own services from implementation to observability.\n\nWhat you bring\nJava and SQL experience, understanding of authorization and automated testing, and a practical approach to maintainability."},
            {"Data Analyst", "Openfield", "Pune", "Data", "Full-time", "On-site", "800000", "1400000", "SQL,Python,Tableau", "Turn operational data into useful decisions. Build dashboards, investigate product metrics, and explain findings to teams.\n\nWhat you bring\nStrong SQL, experience with Python, and the ability to communicate uncertainty clearly."},
            {"UI Engineering Intern", "Forma Studio", "Bengaluru", "Engineering", "Internship", "Remote", "240000", "420000", "React,CSS,Git", "Learn by shipping real interfaces alongside experienced engineers. Improve components, fix accessibility issues, and contribute to a design system.\n\nWhat you bring\nWorking knowledge of HTML, CSS, JavaScript, and Git. Share a project you have built and explain what you learned."},
            {"Growth Marketing Specialist", "Daybreak", "Delhi", "Marketing", "Full-time", "Hybrid", "700000", "1200000", "Analytics,Content,SEO", "Plan content and experiments that help a sustainable consumer brand reach its audience. Measure campaigns and share actionable findings.\n\nWhat you bring\nClear writing, curiosity about customer behavior, and experience with analytics and organic acquisition."},
            {"Full Stack Developer", "Monograph", "Mumbai", "Engineering", "Contract", "Remote", "1500000", "2600000", "React,Node.js,PostgreSQL", "Build features across the frontend and API for a creative workflow platform. Work in a small team with clear ownership.\n\nWhat you bring\nExperience building and deploying web applications, relational database skills, and an interest in product quality."},
            {"Visual Designer", "Daybreak", "Delhi", "Design", "Contract", "Remote", "600000", "1000000", "Figma,Branding,Illustration", "Create a coherent visual language across digital touchpoints. Develop layouts, illustrations, and campaign assets.\n\nWhat you bring\nA strong portfolio, thoughtful typography, and the ability to translate a brief into a clear visual idea."}
        };
        for (var r : rows) db.update("INSERT INTO jobs(title,company,location,category,job_type,workplace,salary_min,salary_max,skills,description,is_demo) VALUES(?,?,?,?,?,?,?,?,?,?,TRUE)",
            r[0],r[1],r[2],r[3],r[4],r[5],Integer.parseInt(r[6]),Integer.parseInt(r[7]),r[8],r[9]);
    }
}
