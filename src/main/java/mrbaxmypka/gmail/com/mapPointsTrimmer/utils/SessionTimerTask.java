package mrbaxmypka.gmail.com.mapPointsTrimmer.utils;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import mrbaxmypka.gmail.com.mapPointsTrimmer.services.MultipartFileService;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

@Slf4j
public class SessionTimerTask extends TimerTask {

    private final Timer timer;
    @Getter
    private final String sessionId;
    private final Map<String, SessionTimerTask> sessionBeaconsCount;
    @Getter
    @Setter
    private int count = 0;
    @Getter
    private boolean isCancelled = false;

    public SessionTimerTask(String sessionId, Timer timer, Map<String, SessionTimerTask> sessionBeaconsCount) {
        this.sessionId = sessionId;
        this.timer = timer;
        this.sessionBeaconsCount = sessionBeaconsCount;
    }

    @Override
    public void run() {
        if (count <= 3) {
            count++;

            //TODO: to remake as log.trace()
            log.warn("Timer count has been increased by 1 up to {} for the session id={}", count, sessionId);

        } else {
            //TODO: to remake as trace()
            log.warn("Timer count = {} for the session id={} so the appropriate process and the temp file is being closed...",
                    count, sessionId);
            MultipartFileService.deleteTempFile(sessionId);
            isCancelled = cancel();
            sessionBeaconsCount.remove(this);
            timer.purge();
            //TODO: to remove
            log.warn("Timer count for the session id={} isCancelled={}", sessionId, isCancelled);
        }
    }
}
