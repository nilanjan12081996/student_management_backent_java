package skyware.product.config.ratelimit.middleware;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;
import skyware.product.config.ratelimit.bucket.UserRequestBucket;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimitFilter extends OncePerRequestFilter {

    private final Map<String, UserRequestBucket> clientBuckets = new ConcurrentHashMap<>();
    private static final int MAX_REQUESTS_PER_MINUTE = 200;
    private static final int MAX_REQUESTS_PER_MINUTE_THROTTLE = 150;
    private static final long WINDOW_MILLIS = 60_000;

//    @Override
//    protected void doFilterInternal(HttpServletRequest request,
//                                    HttpServletResponse response,
//                                    FilterChain filterChain) throws ServletException, IOException {
//
//        // Use client IP as the unique key
//        String clientIp = request.getRemoteAddr();
//
//        // Get or Create a bucket for this IP
//        UserRequestBucket bucket = clientBuckets.computeIfAbsent(clientIp, k -> new UserRequestBucket());
//
//        if (bucket.allowRequest(MAX_REQUESTS_PER_MINUTE, WINDOW_MILLIS)) {
//            // Within limit, proceed to next filter or controller
//            filterChain.doFilter(request, response);
//        } else {
//            // Over limit, return 429 Too Many Requests
//            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
//            response.getWriter().write("Too many requests. Try again in a minute.");
//        }
//    }
        @Override
        protected boolean shouldNotFilter(HttpServletRequest request) {
            String path = request.getRequestURI();
            // Skip rate limiting if the request is for the chat socket
            return path.startsWith("/ws-chat");
        }
        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                throws ServletException, IOException {

            String clientIp = request.getHeader("X-Forwarded-For");
            if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
                clientIp = request.getHeader("X-Real-IP");
            }
            if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
                clientIp = request.getRemoteAddr();
            } else {
                // If multiple IPs are present, the first one is the original client IP
                clientIp = clientIp.split(",")[0].trim();
            }
            UserRequestBucket bucket = clientBuckets.computeIfAbsent(clientIp, k -> new UserRequestBucket());

            // 1. Get the current count
            int count = bucket.getCurrentCount(WINDOW_MILLIS);

            // 2. Hard Block if above 50
            if (count > MAX_REQUESTS_PER_MINUTE) {
                response.setStatus(429);
                response.getWriter().write("Too many requests. Stop spamming.");
                return;
            }

            // 3. Dynamic Throttling if above 20
            long delay = bucket.calculateDelay(count, MAX_REQUESTS_PER_MINUTE_THROTTLE);
            if (delay > 0) {
                try {
                    // Log this so you can see it in console!
                    System.out.println("Throttling IP " + clientIp + " for " + delay + "ms");
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            filterChain.doFilter(request, response);
        }
}
