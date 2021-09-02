package mrbaxmypka.gmail.com.mapPointsTrimmer.utils;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import mrbaxmypka.gmail.com.mapPointsTrimmer.services.MultipartFileService;

import java.util.Map;

@Slf4j
public class SessionTimer implements Runnable {

    @Getter
    private final String sessionId;
    private final Map<String, SessionTimer> sessionBeacons;
    private final MultipartFileService multipartFileService;
    private volatile int count = 0;
    private volatile boolean isCancelled = false;

    public SessionTimer(
            String sessionId, Map<String, SessionTimer> sessionBeacons, MultipartFileService multipartFileService) {
        this.sessionId = sessionId;
        this.sessionBeacons = sessionBeacons;
        this.multipartFileService = multipartFileService;
    }

    @Override
    public synchronized void run() {
        if (this.count <= 3) {
            this.count++;

            //TODO: to remake as log.trace()
            log.warn("Timer count has been increased by 1 up to {} for the session id={}", count, sessionId);

        } else {
            //TODO: to remake as trace()
            log.warn("Timer count = {} for the session id={} so the appropriate process and the temp file is being closed...",
                    count, sessionId);
            multipartFileService.deleteTempFile(sessionId);
            sessionBeacons.remove(this.sessionId);
            //TODO: to remove
            log.warn("Timer count for the session id={} isCancelled={}", sessionId, isCancelled);
            throw new RuntimeException("The SessionTimeTask for id=" + sessionId + " to be stopped!");
        }
    }

    public synchronized int getCount() {
        return count;
    }

/*
    private void setCount(int count) {
        if (!isCancelled()) this.count = count;
    }
*/

    public synchronized boolean isCancelled() {
        return isCancelled;
    }

    public synchronized void setCancelled(boolean cancelled) {
        isCancelled = cancelled;
        if (cancelled) {
            this.count = 4;
        } else {
            this.count = 0;
        }
    }
}
