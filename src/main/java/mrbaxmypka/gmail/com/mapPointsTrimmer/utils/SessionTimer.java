package mrbaxmypka.gmail.com.mapPointsTrimmer.utils;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import mrbaxmypka.gmail.com.mapPointsTrimmer.services.MultipartFileService;
import mrbaxmypka.gmail.com.mapPointsTrimmer.services.WebSessionService;

import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Every User session is associated by sessionId with this object and keeps into {@link #sessionBeacons}.
 * Every object of this class has to be run in a separate thread every {@link WebSessionService#getPERIOD()}
 * to check the {@link #count} if it is less than {@link #MAX_COUNT} or if this session set as {@link #isCancelled} = true.
 * If {@link #MAX_COUNT} has been reached or this session set as cancelled, it interrupts the associated processing by its ThreadId,
 * deletes the appropriate resulting temp file, deletes its sessionId and itself from the common {@link #sessionBeacons} HashMap
 * and throws the {@link RuntimeException} to inform the {@link ScheduledExecutorService} that this task should be removed
 * from its execution queue.
 */
@Slf4j
public class SessionTimer implements Runnable {

    @Getter
    private final String sessionId;
    @Getter(AccessLevel.PROTECTED)
    private final Map<String, SessionTimer> sessionBeacons;
    @Getter(AccessLevel.PROTECTED)
    private final MultipartFileService multipartFileService;
    private volatile int count = -1;
    private final int MAX_COUNT = 3;
    private volatile boolean isCancelled = false;

    public SessionTimer(
            String sessionId, Map<String, SessionTimer> sessionBeacons, MultipartFileService multipartFileService) {
        this.sessionId = sessionId;
        this.sessionBeacons = sessionBeacons;
        this.multipartFileService = multipartFileService;
    }

    @Override
    public synchronized void run() {
        if (Thread.currentThread().isInterrupted() || count > MAX_COUNT || isCancelled) {
            log.trace("Timer count = {} for the session id={} so the appropriate process and the temp file is being closed...",
                    count, sessionId);
            multipartFileService.deleteTempFile(sessionId);
            sessionBeacons.remove(this.sessionId);
            throw new RuntimeException("The SessionTimeTask for id=" + sessionId + " to be stopped!");
        } else {
            count++;
            log.trace("Timer count has been increased by 1 up to {} for the session id={}", count, sessionId);

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
     * @param cancelled If this parameter = true sets the {@link #isCancelled} to 'true'.
     *                  If this parameter = false, sets {@link #count} = 0 to renew it and to inform that the associated
     *                  User session is still alive.
     */
    public synchronized void setCancelled(boolean cancelled) {
        this.isCancelled = cancelled;
        if (!isCancelled) {
            this.count = 0;
        }
    }
}
