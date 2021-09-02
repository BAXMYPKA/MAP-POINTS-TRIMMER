package mrbaxmypka.gmail.com.mapPointsTrimmer.utils;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import mrbaxmypka.gmail.com.mapPointsTrimmer.services.MultipartFileService;

import java.util.Map;

@Slf4j
public class SessionTimer implements Runnable {

    @Getter
    private final String sessionId;
    @Getter(AccessLevel.PROTECTED)
    private final Map<String, SessionTimer> sessionBeacons;
    @Getter(AccessLevel.PROTECTED)
    private final MultipartFileService multipartFileService;
    private volatile int count = -1;
    private volatile boolean isCancelled = false;

    public SessionTimer(
            String sessionId, Map<String, SessionTimer> sessionBeacons, MultipartFileService multipartFileService) {
        this.sessionId = sessionId;
        this.sessionBeacons = sessionBeacons;
        this.multipartFileService = multipartFileService;
    }

    @Override
    public synchronized void run() {
        if (Thread.currentThread().isInterrupted() || count > 3 || isCancelled) {
            //TODO: to remake as trace()
            log.warn("Timer count = {} for the session id={} so the appropriate process and the temp file is being closed...",
                    count, sessionId);
            multipartFileService.deleteTempFile(sessionId);
            sessionBeacons.remove(this.sessionId);
            //TODO: to remove
            log.warn("Timer count for the session id={} isCancelled={}", sessionId, isCancelled);
            throw new RuntimeException("The SessionTimeTask for id=" + sessionId + " to be stopped!");
        } else {
            count++;

            //TODO: to remake as log.trace()
            log.warn("Timer count has been increased by 1 up to {} for the session id={}", count, sessionId);

        }
    }

    public synchronized int getCount() {
        return count;
    }

    protected void setCount(int count) {
        if (!isCancelled()) this.count = count;
    }

    public synchronized boolean isCancelled() {
        return isCancelled;
    }

    /**
     * @param cancelled If this parameter = true:
     *                  1) sets the {@link #isCancelled} to 'true'
     *                  2) sets {@link #count} = 4
     *                  If this parameter = false, sets {@link #count} = 0 to renew it.
     */
    public synchronized void setCancelled(boolean cancelled) {
        this.isCancelled = cancelled;
        if (!isCancelled) {
            this.count = 0;
        }
    }
}
