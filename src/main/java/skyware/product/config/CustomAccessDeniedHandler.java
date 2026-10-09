package skyware.product.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {

        response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("status", false);
        responseBody.put("statusCode", 403);

        // Check what endpoint they tried to hit to customize the message
        String uri = request.getRequestURI();
        if (uri.startsWith("/admin/api/plan")) {
            responseBody.put("message", "Access denied: You do not have Super Admin privileges.");
        } else {
            responseBody.put("message", "Access denied: You are not authorized to view this resource.");
        }

        tools.jackson.databind.ObjectMapper mapper = new tools.jackson.databind.ObjectMapper();
        response.getWriter().write(mapper.writeValueAsString(responseBody));
    }
}
