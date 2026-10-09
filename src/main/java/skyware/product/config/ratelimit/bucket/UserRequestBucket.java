package skyware.product.config.ratelimit.bucket;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class UserRequestBucket {
    private final AtomicInteger count = new AtomicInteger(0);
    private final AtomicLong lastResetTime = new AtomicLong(System.currentTimeMillis());

    public int getCurrentCount(long windowMillis) {
        long now = System.currentTimeMillis();
        long lastReset = lastResetTime.get();

        // 1. Thread-safe check-and-reset
        if (now - lastReset > windowMillis) {
            // Only one thread should be allowed to reset the window
            synchronized (this) {
                // Double-check pattern: ensure another thread didn't reset it while we waited
                if (now - lastResetTime.get() > windowMillis) {
                    count.set(0);
                    lastResetTime.set(now);
                }
            }
        }

        // 2. Atomic increment is already thread-safe
        return count.incrementAndGet();
    }

    public long calculateDelay(int currentCount, int throttleThreshold) {
        if (currentCount <= throttleThreshold) {
            return 0;
        }
        // Every request after the threshold adds 200ms more delay
        return (long) (currentCount - throttleThreshold) * 200;
    }
}
