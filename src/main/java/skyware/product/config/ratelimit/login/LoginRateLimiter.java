package skyware.product.config.ratelimit.login;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LoginRateLimiter {

    // Window of 10 seconds
    private static final long WINDOW_MILLIS = 20_000;

    // Max allowed requests in that window
    private static final int MAX_REQUESTS = 3;

    // Inner class to hold the window start time and the request count
    private static class RequestStats {
        final long startTime;
        final int count;

        RequestStats(long startTime, int count) {
            this.startTime = startTime;
            this.count = count;
        }
    }

    // Key = UserId, Value = RequestStats (Time + Count)
    private final Map<Long, RequestStats> seenRequestsData = new ConcurrentHashMap<>();

    public boolean isBlocked(Long userId) {
        long currentTime = System.currentTimeMillis();

        // compute(): Atomic operation to update the stats
        RequestStats updatedStats = seenRequestsData.compute(userId, (k, existingStats) -> {

            // Case 1: First time seeing user OR Window expired -> Start new window (Count = 1)
            if (existingStats == null || (currentTime - existingStats.startTime) > WINDOW_MILLIS) {
                return new RequestStats(currentTime, 1);
            }

            // Case 2: Within window -> Increment count
            return new RequestStats(existingStats.startTime, existingStats.count + 1);
        });

        // Block if the count exceeds the limit
        return updatedStats.count > MAX_REQUESTS;
    }

    /**
     * Cleanup method to prevent memory leaks
     */
    @Scheduled(fixedRate = 60000)
    public void cleanUp() {
        long now = System.currentTimeMillis();
        // Remove entries where the window has expired
        seenRequestsData.entrySet().removeIf(entry -> (now - entry.getValue().startTime) > WINDOW_MILLIS);
    }
}
