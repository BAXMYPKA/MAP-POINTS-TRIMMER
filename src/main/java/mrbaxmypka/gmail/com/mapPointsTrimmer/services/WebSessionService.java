package mrbaxmypka.gmail.com.mapPointsTrimmer.services;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import mrbaxmypka.gmail.com.mapPointsTrimmer.MapPointsTrimmerApplication;
import mrbaxmypka.gmail.com.mapPointsTrimmer.controllers.BeaconController;
import mrbaxmypka.gmail.com.mapPointsTrimmer.controllers.IndexController;
import mrbaxmypka.gmail.com.mapPointsTrimmer.utils.SessionTimer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Keeps track users sessions. Associates a user-session with the {@link SessionTimer} and periodically checks if the
 * session alive or being renewed. If so (by the lost connection, closed or renewed browser tab etc)
 * the tracking allows to kill the appropriate process and delete the temp file for it as User won't be able to receive
 *  it with the server respond.
 *  If {@link #singleUserMode} = false it just stops and deletes processes and temp files associated with session ids.
 *  In the SingleUserMode it also shut the Application down after idle.
 */
@Slf4j
@Getter
@Component
public class WebSessionService {

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
            checkSingleUserSessions();
        }
    }

    private void checkSingleUserSessions() {
        Runnable checkUserSession = () -> {
            if (sessionBeacons.isEmpty()) {
                log.warn("SessionBeacons in the SingleUserMode isEmpty={}, Application is shutting down...", sessionBeacons.isEmpty());
                shutdownApplication();
            }
        };
        scheduledTimers.scheduleAtFixedRate(checkUserSession, 60, 25, TimeUnit.SECONDS);
    }

    /**
     * When the {@link IndexController#getIndex(Model, HttpSession)} responds with the start page it starts to keep track
     * a User session by its ID with the new {@link SessionTimer} (or the renewed one) and put it in the {@link #sessionBeacons} HashMap.
     * The tracking allows to stop processing and delete temp files associated with the session id as the User won't be able
     * to get it if the connection lost.
     * <p>
     * Checks every 10sec if a User's browser has sent the beacon keep-alive signal onto {@link BeaconController#postBeacon(HttpSession)}
     * and increment the current {@link  SessionTimer} counter by 1 only for the current {@link HttpSession}.
     */
    public void startSessionBeaconTimer(String sessionId) {
        log.trace("Session timer has been started for sessionId={}", sessionId);
        if (!sessionBeacons.containsKey(sessionId)) {
            SessionTimer timerTask = new SessionTimer(sessionId, sessionBeacons, multipartFileService);
            sessionBeacons.put(sessionId, timerTask);
            scheduledTimers.scheduleAtFixedRate(timerTask, 1, 10, TimeUnit.SECONDS);
        } else {
            sessionBeacons.get(sessionId).setCancelled(false);
        }
        log.debug("SessionBeacons contains {} sessions", sessionBeacons.size());
    }

    /**
     * Treats the 'sessionId' when {@link BeaconController#postBeacon(HttpSession)}.
     * {@link BeaconController#postBeacon(HttpSession)} resets it to 0 when the 'navigator.sendBeacon()' is sent.
     * When {@link SessionTimer#getCount()} > 3 the appropriate process and the temp file for this session will be killed by
     * {@link MultipartFileService#deleteTempFile(String)}. If {@link #isSingleUserMode()} = true, the full Application
     * will be shut down.
     *
     * @param sessionId A beacon from the {@link BeaconController#postBeacon(HttpSession)}.
     */
    public void postBeacon(String sessionId) {
        SessionTimer sessionTimer = sessionBeacons.get(sessionId);
        if (sessionTimer != null) {
            sessionTimer.setCancelled(false);
            log.trace("Beacon for sessionId={} has been zeroed", sessionId);
        } else if (singleUserMode) {
            log.info("Restart singleUser sessionId={}", sessionId);
            startSessionBeaconTimer(sessionId);
        } else {
            log.trace("Beacon for an absent sessionId={}", sessionId);
        }
    }

    /**
     * @param sessionId Gets it from the {@link BeaconController#postStopBeacon(HttpSession)} when a User's browser
     *                  Application tab has been closed or renewed. Because after this any files sent before
     *                  cannot be returned to User, so an appropriate processing associated with this sessionId has to be
     *                   killed and temp file has to be deleted.
     */
    public void postStopBeacon(String sessionId) {
        SessionTimer sessionTimer = sessionBeacons.get(sessionId);
        if (sessionTimer != null) {
            sessionTimer.setCancelled(true);
            log.trace("The session id={} has been set as cancelled. SessionBeacons contains {} sessions.",
                    sessionId, sessionBeacons.size());
        }
    }

    public void shutdownApplication() {
        log.warn("The Application will be shut down!");
        scheduledTimers.shutdownNow();
        mapPointsTrimmerApplication.shutDownApp();
    }
}
