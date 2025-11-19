package backend.Email;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class OTPController {
    @Autowired
    private EmailService emailService;
    @PostMapping("/send-otp")
    public String sendOtp(@RequestBody EmailDetails details) {
        String otp = String.format("%05d", (int)(Math.random() * 100000));
        String status = emailService.sendOtpEmail(details.getEmail(), otp);
        return otp; 
    }
}
