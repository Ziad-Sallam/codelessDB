package backend.Email;

public interface EmailService {
    String sendOtpEmail(String email, String otp);
}
