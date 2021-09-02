package mrbaxmypka.gmail.com.mapPointsTrimmer.utils;

import lombok.extern.slf4j.Slf4j;
import mrbaxmypka.gmail.com.mapPointsTrimmer.services.MultipartFileService;

import java.util.Map;

@Slf4j
public class GlobalSessionTimer extends SessionTimer {
    public GlobalSessionTimer(String sessionId, Map<String, SessionTimer> sessionBeacons, MultipartFileService multipartFileService) {
        super(sessionId, sessionBeacons, multipartFileService);
    }

    @Override
    public synchronized void run() {
        if (getCount() <= 3) {
            setCount(getCount() + 1);

            //TODO: to remake as log.trace()
            log.warn("Global Timer count has been increased by 1 up to {} for the session id={}", getCount(), getSessionId());

        } else {
            //TODO: to remake as trace()
            log.warn("Timer count = {} for the session id={} so the appropriate process and the temp file is being closed...",
                    getCount(), getSessionId());
            getMultipartFileService().deleteTempFile(getSessionId());
            getSessionBeacons().remove(getSessionId());
            //TODO: to remove
            log.warn("Timer count for the session id={} isCancelled={}", getSessionId(), isCancelled());

            throw new RuntimeException("The SessionTimeTask for id=" + getSessionId() + " to be stopped!");
        }
    }

}
