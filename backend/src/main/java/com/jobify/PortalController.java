package com.jobify;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class PortalController {
    private final JdbcTemplate db;
    private final PasswordEncoder passwords;
    private final Map<String, Attempt> attempts = new ConcurrentHashMap<>();
    private final String dummyHash;
    private record Attempt(int count, Instant until) {}
    public PortalController(JdbcTemplate db, PasswordEncoder passwords) {
        this.db = db; this.passwords = passwords; dummyHash = passwords.encode(UUID.randomUUID().toString());
    }

    public record Register(@NotBlank @Size(max=80) String username, @NotBlank @Email @Size(max=254) String email,
                           @NotBlank @Size(min=10,max=72) String password, @NotBlank @Pattern(regexp="candidate|recruiter") String role) {}
    public record Login(@NotBlank @Email @Size(max=254) String email, @NotBlank @Size(max=72) String password) {}
    public record Profile(@NotBlank @Size(max=80) String username, @NotNull @Size(max=160) String headline,
                          @NotNull @Size(max=100) String location, @NotNull @Size(max=500) String skills,
                          @NotNull @Size(max=2000) String bio, @NotNull @Size(max=300) String website) {}
    public record JobInput(@NotBlank @Size(max=120) String title, @NotBlank @Size(max=120) String company,
                           @NotBlank @Size(max=100) String location,
                           @NotBlank @Pattern(regexp="Engineering|Design|Data|Marketing|Operations") String category,
                           @NotBlank @Pattern(regexp="Full-time|Part-time|Contract|Internship") String jobType,
                           @NotBlank @Pattern(regexp="Remote|Hybrid|On-site") String workplace,
                           @Min(0) @Max(100000000) int salaryMin, @Min(0) @Max(100000000) int salaryMax,
                           @NotBlank @Size(min=50,max=8000) String description, @NotNull @Size(max=500) String skills) {}
    public record ApplicationInput(@Positive long jobId, @NotBlank @Size(min=20,max=4000) String coverLetter,
                                   @NotNull @Size(max=500) String resumeUrl) {}
    public record StatusInput(@NotBlank @Pattern(regexp="Reviewing|Shortlisted|Rejected") String status) {}
    public record ActiveInput(@NotNull Boolean active) {}

    @GetMapping("/health") public Map<String,String> health() { return Map.of("status","ok"); }
    @GetMapping("/auth/csrf") public Map<String,String> csrf(CsrfToken token) {
        return Map.of("token",token.getToken(),"headerName",token.getHeaderName());
    }
    @PostMapping("/auth/register") @ResponseStatus(HttpStatus.CREATED) @Transactional
    public Map<String,Object> register(@Valid @RequestBody Register input, HttpServletRequest request) {
        throttle(request);
        if (input.password().getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) fail(HttpStatus.BAD_REQUEST,"Password must be at most 72 UTF-8 bytes.");
        String email = input.email().trim().toLowerCase(Locale.ROOT);
        db.update("INSERT INTO users(username,email,password_hash,role) VALUES(?,?,?,?)", input.username().trim(),email,passwords.encode(input.password()),input.role());
        long id = db.queryForObject("SELECT id FROM users WHERE email=?",Long.class,email);
        establish(request,id);
        return user(id);
    }
    @PostMapping("/auth/login") public Map<String,Object> login(@Valid @RequestBody Login input, HttpServletRequest request) {
        throttle(request);
        if (input.password().getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) fail(HttpStatus.BAD_REQUEST,"Password must be at most 72 UTF-8 bytes.");
        var rows = db.queryForList("SELECT id,password_hash FROM users WHERE email=?", input.email().trim().toLowerCase(Locale.ROOT));
        String hash = rows.isEmpty() ? dummyHash : (String)rows.getFirst().get("PASSWORD_HASH");
        boolean matches = passwords.matches(input.password(),hash);
        if (rows.isEmpty() || !matches) fail(HttpStatus.UNAUTHORIZED,"Invalid email or password.");
        long id = ((Number)rows.getFirst().get("ID")).longValue();
        establish(request,id);
        return user(id);
    }
    @PostMapping("/auth/logout") public Map<String,Boolean> logout(HttpServletRequest request) {
        var session = request.getSession(false); if (session != null) session.invalidate();
        return Map.of("success",true);
    }
    @GetMapping("/auth/me") public Map<String,Object> me(HttpServletRequest request) { return user(require(request,null)); }
    @PutMapping("/profile") public Map<String,Object> profile(@Valid @RequestBody Profile p, HttpServletRequest request) {
        long id = require(request,null); safeUrl(p.website());
        db.update("UPDATE users SET username=?,headline=?,location=?,skills=?,bio=?,website=? WHERE id=?",
            p.username().trim(),p.headline().trim(),p.location().trim(),p.skills().trim(),p.bio().trim(),p.website().trim(),id);
        return user(id);
    }

    @GetMapping("/jobs") public Map<String,Object> jobs(
        @RequestParam(defaultValue="") String q, @RequestParam(defaultValue="") String location,
        @RequestParam(defaultValue="") String category, @RequestParam(defaultValue="") String type,
        @RequestParam(defaultValue="") String workplace, @RequestParam(defaultValue="newest") String sort,
        @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="12") int size) {
        if (page < 0 || page > 10000 || size < 1 || size > 50 || q.length()>200 || location.length()>100) fail(HttpStatus.BAD_REQUEST,"Invalid search parameters.");
        var params = new ArrayList<Object>();
        StringBuilder where = new StringBuilder(" WHERE active=TRUE");
        if (!q.isBlank()) {
            where.append(" AND (LOWER(title) LIKE ? ESCAPE '!' OR LOWER(company) LIKE ? ESCAPE '!' OR LOWER(skills) LIKE ? ESCAPE '!')");
            String query = like(q); params.add(query);params.add(query);params.add(query);
        }
        if (!location.isBlank()) { where.append(" AND LOWER(location) LIKE ? ESCAPE '!'"); params.add(like(location)); }
        if (!category.isBlank()) { where.append(" AND category=?");params.add(category); }
        if (!type.isBlank()) { where.append(" AND job_type=?");params.add(type); }
        if (!workplace.isBlank()) { where.append(" AND workplace=?");params.add(workplace); }
        long total = db.queryForObject("SELECT COUNT(*) FROM jobs"+where,Long.class,params.toArray());
        String ordering = sort.equals("salary") ? "salary_max DESC,id DESC" : "created_at DESC,id DESC";
        params.add(size);params.add(page*size);
        var items = db.query("SELECT * FROM jobs"+where+" ORDER BY "+ordering+" LIMIT ? OFFSET ?",this::job,params.toArray());
        return Map.of("items",items,"total",total,"page",page,"size",size);
    }
    @GetMapping("/jobs/{id}") public Map<String,Object> detail(@PathVariable long id) { return findJob(id); }
    @GetMapping("/stats") public Map<String,Object> stats() {
        return Map.of("jobs",db.queryForObject("SELECT COUNT(*) FROM jobs WHERE active=TRUE",Long.class),
            "companies",db.queryForObject("SELECT COUNT(DISTINCT company) FROM jobs WHERE active=TRUE",Long.class),
            "remote",db.queryForObject("SELECT COUNT(*) FROM jobs WHERE active=TRUE AND workplace='Remote'",Long.class));
    }
    @GetMapping("/saved") public List<Map<String,Object>> saved(HttpServletRequest request) {
        return db.query("SELECT j.* FROM saved_jobs s JOIN jobs j ON j.id=s.job_id WHERE s.user_id=? ORDER BY s.created_at DESC",this::job,require(request,"candidate"));
    }
    @PostMapping("/saved/{id}") @Transactional public Map<String,Boolean> save(@PathVariable long id, HttpServletRequest request) {
        long userId = require(request,"candidate"); findJob(id);
        db.update("MERGE INTO saved_jobs(user_id,job_id) KEY(user_id,job_id) VALUES(?,?)",userId,id);
        return Map.of("saved",true);
    }
    @DeleteMapping("/saved/{id}") public Map<String,Boolean> unsave(@PathVariable long id,HttpServletRequest request) {
        db.update("DELETE FROM saved_jobs WHERE user_id=? AND job_id=?",require(request,"candidate"),id);
        return Map.of("saved",false);
    }
    @GetMapping("/applications") public List<Map<String,Object>> applications(HttpServletRequest request) {
        long id=require(request,"candidate");
        return db.query("SELECT a.*,j.title,j.company,j.is_demo FROM applications a JOIN jobs j ON j.id=a.job_id WHERE a.user_id=? ORDER BY a.created_at DESC",this::application,id);
    }
    @PostMapping("/applications") @ResponseStatus(HttpStatus.CREATED) @Transactional
    public Map<String,Object> apply(@Valid @RequestBody ApplicationInput input,HttpServletRequest request) {
        long id=require(request,"candidate"); var job=findJob(input.jobId()); safeUrl(input.resumeUrl());
        if (!(Boolean)job.get("active")) fail(HttpStatus.CONFLICT,"This job is closed.");
        db.update("INSERT INTO applications(user_id,job_id,cover_letter,resume_url) VALUES(?,?,?,?)",id,input.jobId(),input.coverLetter().trim(),input.resumeUrl().trim());
        return Map.of("status","Submitted","demo",job.get("demo"));
    }
    @DeleteMapping("/applications/{id}") public Map<String,Boolean> withdraw(@PathVariable long id,HttpServletRequest request) {
        int updated=db.update("UPDATE applications SET status='Withdrawn' WHERE id=? AND user_id=?",id,require(request,"candidate"));
        if(updated==0) fail(HttpStatus.NOT_FOUND,"Application not found.");
        return Map.of("success",true);
    }

    @GetMapping("/recruiter/jobs") public List<Map<String,Object>> ownJobs(HttpServletRequest request) {
        return db.query("SELECT * FROM jobs WHERE recruiter_id=? ORDER BY created_at DESC",this::job,require(request,"recruiter"));
    }
    @PostMapping("/recruiter/jobs") @ResponseStatus(HttpStatus.CREATED) @Transactional
    public Map<String,Object> postJob(@Valid @RequestBody JobInput j,HttpServletRequest request) {
        long recruiter=require(request,"recruiter"); validateJob(j);
        var key = new org.springframework.jdbc.support.GeneratedKeyHolder();
        db.update(connection -> {
            var s=connection.prepareStatement("INSERT INTO jobs(recruiter_id,title,company,location,category,job_type,workplace,salary_min,salary_max,description,skills) VALUES(?,?,?,?,?,?,?,?,?,?,?)",new String[]{"id"});
            Object[] values={recruiter,j.title().trim(),j.company().trim(),j.location().trim(),j.category(),j.jobType(),j.workplace(),j.salaryMin(),j.salaryMax(),j.description().trim(),j.skills().trim()};
            for(int i=0;i<values.length;i++) s.setObject(i+1,values[i]); return s;
        },key);
        return findJob(Objects.requireNonNull(key.getKey()).longValue());
    }
    @PutMapping("/recruiter/jobs/{id}") @Transactional public Map<String,Object> editJob(@PathVariable long id,@Valid @RequestBody JobInput j,HttpServletRequest request) {
        long recruiter=require(request,"recruiter"); ownJob(id,recruiter); validateJob(j);
        db.update("UPDATE jobs SET title=?,company=?,location=?,category=?,job_type=?,workplace=?,salary_min=?,salary_max=?,description=?,skills=? WHERE id=? AND recruiter_id=?",
            j.title().trim(),j.company().trim(),j.location().trim(),j.category(),j.jobType(),j.workplace(),j.salaryMin(),j.salaryMax(),j.description().trim(),j.skills().trim(),id,recruiter);
        return findJob(id);
    }
    @PatchMapping("/recruiter/jobs/{id}") public Map<String,Object> active(@PathVariable long id,@Valid @RequestBody ActiveInput input,HttpServletRequest request) {
        long recruiter=require(request,"recruiter"); ownJob(id,recruiter);
        db.update("UPDATE jobs SET active=? WHERE id=? AND recruiter_id=?",input.active(),id,recruiter);return findJob(id);
    }
    @GetMapping("/recruiter/applications") public List<Map<String,Object>> received(HttpServletRequest request) {
        long recruiter=require(request,"recruiter");
        return db.query("SELECT a.*,j.title,j.company,j.is_demo,u.username,u.email,u.headline,u.skills,u.website FROM applications a JOIN jobs j ON j.id=a.job_id JOIN users u ON u.id=a.user_id WHERE j.recruiter_id=? ORDER BY a.created_at DESC",
            (rs,n) -> { var map=application(rs,n);map.put("candidate",rs.getString("username"));map.put("email",rs.getString("email"));map.put("headline",rs.getString("headline"));map.put("skills",rs.getString("skills"));map.put("website",rs.getString("website"));return map; },recruiter);
    }
    @PatchMapping("/recruiter/applications/{id}") @Transactional
    public Map<String,Boolean> status(@PathVariable long id,@Valid @RequestBody StatusInput input,HttpServletRequest request) {
        long recruiter=require(request,"recruiter");
        int updated=db.update("UPDATE applications SET status=? WHERE id=? AND status<>'Withdrawn' AND job_id IN (SELECT id FROM jobs WHERE recruiter_id=?)",input.status(),id,recruiter);
        if(updated==0) fail(HttpStatus.NOT_FOUND,"Application not found or withdrawn.");return Map.of("success",true);
    }

    private void validateJob(JobInput j) { if(j.salaryMax()<j.salaryMin()) fail(HttpStatus.BAD_REQUEST,"Maximum salary must be at least the minimum."); }
    private void ownJob(long id,long recruiter) {
        if(db.queryForObject("SELECT COUNT(*) FROM jobs WHERE id=? AND recruiter_id=?",Integer.class,id,recruiter)==0) fail(HttpStatus.NOT_FOUND,"Job not found.");
    }
    private void safeUrl(String url) {
        if(url.isBlank()) return;
        try { URI uri=URI.create(url.trim()); if(!Set.of("https","http").contains(uri.getScheme()) || uri.getHost()==null || uri.getUserInfo()!=null) throw new IllegalArgumentException(); }
        catch(IllegalArgumentException e) { fail(HttpStatus.BAD_REQUEST,"Please use a valid http or https URL."); }
    }
    private String like(String s) { return "%"+s.trim().toLowerCase(Locale.ROOT).replace("!","!!").replace("%","!%").replace("_","!_")+"%"; }
    private synchronized void throttle(HttpServletRequest request) {
        Instant now=Instant.now(); attempts.entrySet().removeIf(e -> e.getValue().until().isBefore(now));
        String key=request.getRemoteAddr(); Attempt attempt=attempts.get(key);
        if(attempt!=null && attempt.count()>=20) fail(HttpStatus.TOO_MANY_REQUESTS,"Too many attempts. Please try again in 15 minutes.");
        attempts.put(key,new Attempt(attempt==null?1:attempt.count()+1,attempt==null?now.plusSeconds(900):attempt.until()));
    }
    private void establish(HttpServletRequest request,long id) {
        HttpSession old=request.getSession(false); if(old!=null) old.invalidate(); request.getSession(true).setAttribute("userId",id);
    }
    private long require(HttpServletRequest request,String role) {
        var session=request.getSession(false);
        if(session==null || !(session.getAttribute("userId") instanceof Long)) fail(HttpStatus.UNAUTHORIZED,"Please log in to continue.");
        long id=(Long)session.getAttribute("userId"); var u=user(id);
        if(role!=null && !role.equals(u.get("role"))) fail(HttpStatus.FORBIDDEN,"This action requires a "+role+" account."); return id;
    }
    private Map<String,Object> user(long id) {
        var rows=db.query("SELECT id,username,email,role,headline,location,skills,bio,website FROM users WHERE id=?",(rs,n)->{
            Map<String,Object> m=new LinkedHashMap<>();m.put("id",rs.getLong("id"));
            for(String field:List.of("username","email","role","headline","location","skills","bio","website")) m.put(field,rs.getString(field));return m;
        },id);
        if(rows.isEmpty()) fail(HttpStatus.UNAUTHORIZED,"Please log in to continue.");return rows.getFirst();
    }
    private Map<String,Object> findJob(long id) {
        var rows=db.query("SELECT * FROM jobs WHERE id=?",this::job,id);
        if(rows.isEmpty()) fail(HttpStatus.NOT_FOUND,"Job not found.");return rows.getFirst();
    }
    private Map<String,Object> job(ResultSet rs,int row) throws SQLException {
        Map<String,Object> m=new LinkedHashMap<>();m.put("id",rs.getLong("id"));
        for(String field:List.of("title","company","location","category","workplace","description","skills"))m.put(field,rs.getString(field));
        m.put("jobType",rs.getString("job_type"));m.put("salaryMin",rs.getInt("salary_min"));m.put("salaryMax",rs.getInt("salary_max"));
        m.put("active",rs.getBoolean("active"));m.put("demo",rs.getBoolean("is_demo"));m.put("createdAt",rs.getTimestamp("created_at").toInstant());return m;
    }
    private Map<String,Object> application(ResultSet rs,int row) throws SQLException {
        Map<String,Object> m=new LinkedHashMap<>();m.put("id",rs.getLong("id"));m.put("jobId",rs.getLong("job_id"));
        m.put("title",rs.getString("title"));m.put("company",rs.getString("company"));m.put("status",rs.getString("status"));
        m.put("coverLetter",rs.getString("cover_letter"));m.put("resumeUrl",rs.getString("resume_url"));m.put("demo",rs.getBoolean("is_demo"));
        m.put("createdAt",rs.getTimestamp("created_at").toInstant());return m;
    }
    private static void fail(HttpStatus status,String message) { throw new ResponseStatusException(status,message); }
}
