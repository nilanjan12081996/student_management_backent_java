package skyware.product.config.ratelimit.otpratelimit;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OtpRateLimiter {

    // Window of 60 seconds
    private static final long WINDOW_MILLIS = 60_000;

    // Key = UserId (Long), Value = Timestamp (Long)
    private final Map<Long, Long> seenRequests = new ConcurrentHashMap<>();

    public boolean isBlocked(Long userId) {
        long currentTime = System.currentTimeMillis();

        // compute(): Atomic operation to check and update the map
        Long firstSeenAt = seenRequests.compute(userId, (k, v) -> {
            if (v == null || (currentTime - v) > WINDOW_MILLIS) {
                // Not seen before OR the previous one is outside the window
                return currentTime;
            }
            return v; // Keep the original timestamp to maintain the window
        });

        // If the timestamp in the map is NOT the current time we just tried to set,
        // it means an entry already existed and is valid. -> BLOCK IT.
        return firstSeenAt != currentTime;
    }

    /**
     * Cleanup method to prevent memory leaks
     */
    @Scheduled(fixedRate = 60000)
    public void cleanUp() {
        long now = System.currentTimeMillis();
        System.out.println("Cleaning up OTP cache...");
        seenRequests.entrySet().removeIf(entry -> (now - entry.getValue()) > WINDOW_MILLIS);
    }
}
