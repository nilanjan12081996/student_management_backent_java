package skyware.product.skywareapi.teacher.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeacherEmailService {

    private final JavaMailSender mailSender;

    public void sendOtpEmail(String toEmail, String teacherName, Integer otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Teacher Registration - OTP Verification");

            String displayName = (teacherName != null && !teacherName.trim().isEmpty()) ? teacherName : "Teacher";

            String htmlContent = "<div style='font-family: Arial, sans-serif; padding: 25px; color: #333; max-width: 600px; margin: auto; border: 1px solid #e0e0e0; border-radius: 8px;'>"
                    + "<h2 style='color: #4f46e5; margin-bottom: 10px;'>Verify Your Teacher Registration</h2>"
                    + "<p>Hello <strong>" + displayName + "</strong>,</p>"
                    + "<p>Thank you for registering. Please use the following One-Time Password (OTP) to complete your verification:</p>"
                    + "<div style='text-align: center; margin: 30px 0;'>"
                    + "<span style='display: inline-block; font-size: 32px; font-weight: bold; letter-spacing: 6px; padding: 12px 24px; background-color: #f5f3ff; color: #4f46e5; border: 2px dashed #4f46e5; border-radius: 8px;'>" + otp + "</span>"
                    + "</div>"
                    + "<p style='color: #666; font-size: 14px;'>This OTP is valid for registration verification. Do not share this OTP with anyone.</p>"
                    + "<hr style='border: none; border-top: 1px solid #eee; margin: 20px 0;'/>"
                    + "<p style='color: #999; font-size: 12px;'>If you did not request this registration, please ignore this email.</p>"
                    + "</div>";

            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("Teacher registration OTP email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send OTP email to {}: {}", toEmail, e.getMessage());
        }
    }
}
