package hackathon.project.fraud_detection.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Slf4j
@RequiredArgsConstructor
public class AdminPanelController {

    @GetMapping("/main")
    public String main() { return "main"; }

    @GetMapping("/signin")
    public String login() {
        return "signin";
    }

    @GetMapping("/signup")
    public String register() {
        return "signup";
    }

    @GetMapping("/index")
    public String index() {
        return "index";
    }

    @GetMapping("/rules")
    public String rules() {
        return "rules";
    }

    @GetMapping("/transactions")
    public String transactions() {
        return "transactions";
    }
}
