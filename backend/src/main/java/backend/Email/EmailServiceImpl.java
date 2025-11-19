package backend.Email;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {
    @Autowired
    private JavaMailSender mailSender;
    @Override
    public String sendOtpEmail(String email, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("legendboudy@gmail.com");
            message.setTo(email);
            message.setSubject("Your Password Reset OTP");
            message.setText("Your OTP is: " + otp + "\nThis code is valid for 10 minutes.");
            mailSender.send(message);
            return "OTP sent successfully!";
        } catch (Exception e) {
            return "Error sending email: " + e.getMessage();
        }
    }
}
