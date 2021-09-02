package mrbaxmypka.gmail.com.mapPointsTrimmer.services;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import mrbaxmypka.gmail.com.mapPointsTrimmer.MapPointsTrimmerApplication;
import mrbaxmypka.gmail.com.mapPointsTrimmer.controllers.BeaconController;
import mrbaxmypka.gmail.com.mapPointsTrimmer.utils.SessionTimer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Getter
@Component
public class WebSessionService {

    /**
     * This 'final static' implementation is ONLY for localhost using!
     * The real server implementation has to have a local thread variable distiguished by SESSIONID.
     */
    private final static AtomicInteger beaconsCount = new AtomicInteger(0);
    private final static Map<String, SessionTimer> sessionBeacons = new ConcurrentHashMap<>(2);
    private final static ScheduledExecutorService scheduledTimers = Executors.newScheduledThreadPool(10);
    private final boolean singleUserMode;
    private final MapPointsTrimmerApplication mapPointsTrimmerApplication;
    private final MultipartFileService multipartFileService;

    @Autowired
    public WebSessionService(MapPointsTrimmerApplication mapPointsTrimmerApplication,
                             MultipartFileService multipartFileService,
                             @Value("${trimmer.single-user-mode:true}") boolean singleUserMode) {
        this.mapPointsTrimmerApplication = mapPointsTrimmerApplication;
        this.multipartFileService = multipartFileService;
        this.singleUserMode = singleUserMode;
        log.warn("SingleUserMode={}", singleUserMode);
        if (singleUserMode) {
            checkUserSession();
        }
    }

    private void checkUserSession() {
        Runnable checkUserSession = () -> {
            //TODO: to delete
            log.warn("SessionBeacons isEmpty={}", sessionBeacons.isEmpty());

            if (sessionBeacons.isEmpty()) {
                log.warn("SessionBeacons isEmpty={}, Application is shutting down...", sessionBeacons.isEmpty());
                shutdownApplication();
            }
        };
        scheduledTimers.scheduleAtFixedRate(checkUserSession, 60, 25, TimeUnit.SECONDS);
    }

    /**
     * Real serverside multiuser-oriented method!
     * Checks every 15sec if a User's browser has sent the beacon keep-alive signal onto {@link BeaconController#postBeacon(HttpSession)}
     * and increment the {@link #sessionBeacons} counter by 1 only for the current {@link HttpSession}.
     * {@link BeaconController#postBeacon(HttpSession)} resets it to 0 when the 'navigator.sendBeacon()' is sent.
     * When {@link #sessionBeacons )} > 4 the appropriate process and the temp file for this session will be killed be
     * {@link MultipartFileService#deleteTempFile(String)}.
     */
    public void startSessionBeaconTimer(String sessionId) {

        //TODO: to make as trace
        log.warn("Session timer has been started for sessionId={}", sessionId);

        if (!sessionBeacons.containsKey(sessionId)) {
            SessionTimer timerTask = new SessionTimer(sessionId, sessionBeacons, multipartFileService);
            sessionBeacons.put(sessionId, timerTask);
            scheduledTimers.scheduleAtFixedRate(timerTask, 1, 10, TimeUnit.SECONDS);
        } else {
            sessionBeacons.get(sessionId).setCancelled(false);
        }
        //TODO: to make as trace
        log.warn("SessionBeacons contains {} sessions", sessionBeacons.size());
    }

    public void postBeacon(String sessionId) {
        SessionTimer sessionTimer = sessionBeacons.get(sessionId);
        if (sessionTimer != null) {
            sessionTimer.setCancelled(false);

            //TODO: to make as trace
            log.warn("Beacon for sessionId={} has been zeroed", sessionId);
        } else if (singleUserMode) {
            log.info("Restart singleUser sessionId={}", sessionId);
            startSessionBeaconTimer(sessionId);
        } else {
            //TODO: make as trace
            log.warn("Beacon for an absent sessionId={}", sessionId);
        }
    }

    public void postStopBeacon(String sessionId) {
        SessionTimer sessionTimer = sessionBeacons.get(sessionId);
        if (sessionTimer != null) {
            sessionTimer.setCancelled(true);

            //TODO: to make as trace
            log.warn("The session id={} has been set as cancelled. SessionBeacons contains {} sessions.",
                    sessionId, sessionBeacons.size());
        }
    }

    public void shutdownApplication() {
        log.warn("The Application will be shut down!");
        scheduledTimers.shutdownNow();
        mapPointsTrimmerApplication.shutDownApp();
    }
}
