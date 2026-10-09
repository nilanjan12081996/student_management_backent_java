package skyware.product.skywareapi.superadmin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendVerificationEmail(String toEmail, String generatedPassword, String verifyLink) {
        try {
            jakarta.mail.internet.MimeMessage message = mailSender.createMimeMessage();
            org.springframework.mail.javamail.MimeMessageHelper helper = new org.springframework.mail.javamail.MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Your Admin Account has been created");

            String htmlContent = "<div style='font-family: Arial, sans-serif; padding: 20px; color: #333;'>"
                    + "<h2>Welcome!</h2>"
                    + "<p>Your admin account has been created successfully.</p>"
                    + "<p><strong>Your Login Password is:</strong> " + generatedPassword + "</p>"
                    + "<p>Please verify your account by clicking the button below:</p>"
                    + "<a href='" + verifyLink + "' target='_blank' style='"
                    + "display: inline-block; padding: 10px 20px; font-size: 16px; "
                    + "color: #fff; background-color: #007bff; text-decoration: none; "
                    + "border-radius: 5px; margin-top: 10px;'>Verify Account</a>"
                    + "<p style='margin-top: 20px;'>Thank you.</p>"
                    + "</div>";

            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (jakarta.mail.MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }

    public void sendResetPasswordEmail(String toEmail, String resetLink) {
        try {
            jakarta.mail.internet.MimeMessage message = mailSender.createMimeMessage();
            org.springframework.mail.javamail.MimeMessageHelper helper = new org.springframework.mail.javamail.MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Reset Your Password");

            String htmlContent = "<div style='font-family: Arial, sans-serif; padding: 20px; color: #333;'>"
                    + "<h2>Reset Password</h2>"
                    + "<p>You requested to reset your password.</p>"
                    + "<p>Click the button below to set a new password:</p>"
                    + "<a href='" + resetLink + "' target='_blank' style='"
                    + "display: inline-block; padding: 10px 20px; font-size: 16px; "
                    + "color: #fff; background-color: #dc3545; text-decoration: none; "
                    + "border-radius: 5px; margin-top: 10px;'>Change Password</a>"
                    + "<p style='margin-top: 20px;'>If you didn't request this, you can ignore this email.</p>"
                    + "</div>";

            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (jakarta.mail.MessagingException e) {
            throw new RuntimeException("Failed to send reset password email", e);
        }
    }
}
