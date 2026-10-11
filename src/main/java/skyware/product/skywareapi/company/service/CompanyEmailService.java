package skyware.product.skywareapi.company.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyEmailService {

    private final JavaMailSender mailSender;

    public void sendOtpEmail(String toEmail, String companyName, Integer otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Company Registration - OTP Verification");

            String displayName = (companyName != null && !companyName.trim().isEmpty()) ? companyName : "Company";

            String htmlContent = "<div style='font-family: Arial, sans-serif; padding: 25px; color: #333; max-width: 600px; margin: auto; border: 1px solid #e0e0e0; border-radius: 8px;'>"
                    + "<h2 style='color: #007bff; margin-bottom: 10px;'>Verify Your Company Registration</h2>"
                    + "<p>Hello <strong>" + displayName + "</strong>,</p>"
                    + "<p>Thank you for registering. Please use the following One-Time Password (OTP) to complete your verification:</p>"
                    + "<div style='text-align: center; margin: 30px 0;'>"
                    + "<span style='display: inline-block; font-size: 32px; font-weight: bold; letter-spacing: 6px; padding: 12px 24px; background-color: #f1f7ff; color: #007bff; border: 2px dashed #007bff; border-radius: 8px;'>" + otp + "</span>"
                    + "</div>"
                    + "<p style='color: #666; font-size: 14px;'>This OTP is valid for registration verification. Do not share this OTP with anyone.</p>"
                    + "<hr style='border: none; border-top: 1px solid #eee; margin: 20px 0;'/>"
                    + "<p style='color: #999; font-size: 12px;'>If you did not request this registration, please ignore this email.</p>"
                    + "</div>";

            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("Company registration OTP email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send OTP email to {}: {}", toEmail, e.getMessage());
        }
    }

    public void sendActivationEmail(String toEmail, String companyName, String activationLink) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Activate Your Company Account");

            String displayName = (companyName != null && !companyName.trim().isEmpty()) ? companyName : "Company";

            String htmlContent = "<div style='font-family: Arial, sans-serif; padding: 30px; color: #333; max-width: 600px; margin: auto; border: 1px solid #e0e0e0; border-radius: 10px; background-color: #ffffff;'>"
                    + "<h2 style='color: #28a745; margin-bottom: 12px;'>Account Activation</h2>"
                    + "<p>Hello <strong>" + displayName + "</strong>,</p>"
                    + "<p>Your OTP verification was successful. Please click the button below to activate your company account:</p>"
                    + "<div style='text-align: center; margin: 35px 0;'>"
                    + "<a href='" + activationLink + "' target='_blank' style='"
                    + "display: inline-block; padding: 14px 32px; font-size: 16px; font-weight: bold; "
                    + "color: #ffffff; background-color: #28a745; text-decoration: none; "
                    + "border-radius: 6px; box-shadow: 0 4px 6px rgba(40,167,69,0.2);'>Activate your account</a>"
                    + "</div>"
                    + "<p style='color: #666; font-size: 14px;'>Or copy and paste this link in your browser:</p>"
                    + "<p style='font-size: 12px; color: #007bff; word-break: break-all;'>" + activationLink + "</p>"
                    + "<hr style='border: none; border-top: 1px solid #eee; margin: 25px 0;'/>"
                    + "<p style='color: #999; font-size: 12px;'>If you did not request this, please ignore this email.</p>"
                    + "</div>";

            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("Company activation email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send activation email to {}: {}", toEmail, e.getMessage());
        }
    }
}
