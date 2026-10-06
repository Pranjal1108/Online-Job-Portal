package com.jobify;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import java.util.Map;
import java.util.UUID;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties={"spring.datasource.url=jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"})
@AutoConfigureMockMvc
class PortalIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    private String body(Object value) throws Exception {return json.writeValueAsString(value);}
    private MockHttpSession register(String role) throws Exception {
        var result=mvc.perform(post("/api/auth/register").with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content(body(Map.of("username","Integration Tester","email",UUID.randomUUID()+"@example.test","password","A-strong-password-123","role",role))))
            .andExpect(status().isCreated()).andExpect(jsonPath("password_hash").doesNotExist()).andReturn();
        return (MockHttpSession)result.getRequest().getSession(false);
    }
    private Map<String,Object> listing(String title) {
        return Map.of("title",title,"company","Test Company","location","Bengaluru","category","Engineering","jobType","Full-time","workplace","Remote",
            "salaryMin",1000000,"salaryMax",2000000,"description","A genuine test role with enough detail to validate the complete end-to-end hiring workflow.","skills","Java,React");
    }
    @Test void publicSearchAndInputValidation() throws Exception {
        mvc.perform(get("/api/jobs").param("q","React").param("workplace","Remote")).andExpect(status().isOk())
            .andExpect(jsonPath("total").value(org.hamcrest.Matchers.greaterThanOrEqualTo(2)))
            .andExpect(jsonPath("$.items[*].workplace").value(org.hamcrest.Matchers.everyItem(org.hamcrest.Matchers.is("Remote"))));
        mvc.perform(get("/api/jobs").param("q","%" )).andExpect(status().isOk()).andExpect(jsonPath("total").value(0));
        mvc.perform(get("/api/jobs").param("size","999")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/jobs/999999")).andExpect(status().isNotFound());
    }
    @Test void requiresAuthenticationAndCsrf() throws Exception {
        mvc.perform(get("/api/applications")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isForbidden());
        mvc.perform(post("/api/auth/register").with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isBadRequest());
        var candidate=register("candidate");
        mvc.perform(post("/api/recruiter/jobs").session(candidate).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body(listing("Forbidden role")))).andExpect(status().isForbidden());
    }
    @Test void hiringWorkflowEnforcesOwnershipAndDuplicateApplications() throws Exception {
        var recruiter=register("recruiter"); var otherRecruiter=register("recruiter"); var candidate=register("candidate"); var otherCandidate=register("candidate");
        var result=mvc.perform(post("/api/recruiter/jobs").session(recruiter).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body(listing("Ownership test"))))
            .andExpect(status().isCreated()).andReturn();
        long jobId=json.readTree(result.getResponse().getContentAsString()).get("id").asLong();
        mvc.perform(patch("/api/recruiter/jobs/"+jobId).session(otherRecruiter).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"active\":false}")).andExpect(status().isNotFound());
        mvc.perform(post("/api/saved/"+jobId).session(candidate).with(csrf())).andExpect(status().isOk());
        mvc.perform(post("/api/saved/"+jobId).session(candidate).with(csrf())).andExpect(status().isOk());
        mvc.perform(get("/api/saved").session(candidate)).andExpect(jsonPath("$.length()").value(1));
        mvc.perform(get("/api/saved").session(otherCandidate)).andExpect(jsonPath("$.length()").value(0));
        String application=body(Map.of("jobId",jobId,"coverLetter","I have experience building reliable Java applications.","resumeUrl","https://example.test/resume"));
        mvc.perform(post("/api/applications").session(candidate).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(application)).andExpect(status().isCreated());
        mvc.perform(post("/api/applications").session(candidate).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(application)).andExpect(status().isConflict());
        var applications=mvc.perform(get("/api/applications").session(candidate)).andReturn();
        long applicationId=json.readTree(applications.getResponse().getContentAsString()).get(0).get("id").asLong();
        mvc.perform(delete("/api/applications/"+applicationId).session(otherCandidate).with(csrf())).andExpect(status().isNotFound());
        mvc.perform(patch("/api/recruiter/applications/"+applicationId).session(otherRecruiter).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"Shortlisted\"}")).andExpect(status().isNotFound());
        mvc.perform(patch("/api/recruiter/applications/"+applicationId).session(recruiter).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"Shortlisted\"}")).andExpect(status().isOk());
        mvc.perform(get("/api/applications").session(candidate)).andExpect(jsonPath("$[0].status").value("Shortlisted"));
        mvc.perform(delete("/api/applications/"+applicationId).session(candidate).with(csrf())).andExpect(status().isOk());
        mvc.perform(patch("/api/recruiter/applications/"+applicationId).session(recruiter).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"Reviewing\"}")).andExpect(status().isNotFound());
        mvc.perform(patch("/api/recruiter/jobs/"+jobId).session(recruiter).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"active\":false}")).andExpect(status().isOk());
        mvc.perform(post("/api/applications").session(otherCandidate).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(application)).andExpect(status().isConflict());
    }
    @Test void loginLogoutProfileAndUrlValidation() throws Exception {
        String email=UUID.randomUUID()+"@example.test";
        mvc.perform(post("/api/auth/register").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body(Map.of("username","Account Test","email",email,"password","A-strong-password-123","role","candidate")))).andExpect(status().isCreated());
        mvc.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body(Map.of("email",email,"password","incorrect")))).andExpect(status().isUnauthorized());
        var login=mvc.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body(Map.of("email",email,"password","A-strong-password-123")))).andExpect(status().isOk()).andReturn();
        var session=(MockHttpSession)login.getRequest().getSession(false);
        String profile=body(Map.of("username","Updated Name","headline","Java Developer","location","Mumbai","skills","Java,SQL","bio","My experience","website","javascript:alert(1)"));
        mvc.perform(put("/api/profile").session(session).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(profile)).andExpect(status().isBadRequest());
        mvc.perform(put("/api/profile").session(session).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(profile.replace("javascript:alert(1)","https://example.test"))).andExpect(status().isOk()).andExpect(jsonPath("username").value("Updated Name"));
        mvc.perform(post("/api/auth/logout").session(session).with(csrf())).andExpect(status().isOk());
        assertTrue(session.isInvalid());
    }
    @Test void rejectsOversizedUtf8Passwords() throws Exception {
        for (String route : new String[]{"register","login"}) {
            var input=new java.util.HashMap<String,Object>();
            input.put("email",UUID.randomUUID()+"@example.test");input.put("password","界".repeat(30));
            if(route.equals("register")){input.put("username","Unicode Tester");input.put("role","candidate");}
            mvc.perform(post("/api/auth/"+route).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body(input)))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("message").value("Password must be at most 72 UTF-8 bytes."));
        }
    }
    @Test void editingListingsPreservesOwnershipAndValidatesSalary() throws Exception {
        var owner=register("recruiter");var stranger=register("recruiter");
        var created=mvc.perform(post("/api/recruiter/jobs").session(owner).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body(listing("Original title"))))
            .andExpect(status().isCreated()).andReturn();
        long id=json.readTree(created.getResponse().getContentAsString()).get("id").asLong();
        mvc.perform(put("/api/recruiter/jobs/"+id).session(stranger).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body(listing("Unauthorized edit"))))
            .andExpect(status().isNotFound());
        var invalid=new java.util.HashMap<>(listing("Invalid salary"));invalid.put("salaryMin",3000000);
        mvc.perform(put("/api/recruiter/jobs/"+id).session(owner).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body(invalid)))
            .andExpect(status().isBadRequest());
        mvc.perform(get("/api/jobs/"+id)).andExpect(jsonPath("title").value("Original title"));
        mvc.perform(put("/api/recruiter/jobs/"+id).session(owner).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body(listing("Updated title"))))
            .andExpect(status().isOk()).andExpect(jsonPath("title").value("Updated title"));
    }
    @Test void searchPaginationAndSalarySorting() throws Exception {
        var first=mvc.perform(get("/api/jobs").param("size","1").param("sort","salary")).andExpect(status().isOk()).andReturn();
        var second=mvc.perform(get("/api/jobs").param("size","1").param("page","1").param("sort","salary")).andExpect(status().isOk()).andReturn();
        var a=json.readTree(first.getResponse().getContentAsString());var b=json.readTree(second.getResponse().getContentAsString());
        assertEquals(1,a.get("items").size());assertEquals(a.get("total"),b.get("total"));
        assertNotEquals(a.at("/items/0/id"),b.at("/items/0/id"));
        assertTrue(a.at("/items/0/salaryMax").asInt()>=b.at("/items/0/salaryMax").asInt());
        mvc.perform(get("/api/jobs").param("page","-1")).andExpect(status().isBadRequest());
    }
}
