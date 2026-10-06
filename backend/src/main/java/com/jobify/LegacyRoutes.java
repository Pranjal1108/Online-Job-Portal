package com.jobify;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
@Controller
public class LegacyRoutes {
    @GetMapping("/Homepage.html") public String home() { return "forward:/index.html"; }
    @GetMapping("/Loginpage.html") public String login() { return "forward:/index.html"; }
}
