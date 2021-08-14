package mrbaxmypka.gmail.com.mapPointsTrimmer.controllers;

import lombok.extern.slf4j.Slf4j;
import mrbaxmypka.gmail.com.mapPointsTrimmer.utils.SessionTimerTask;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Slf4j
@Controller
public class BeaconController extends AbstractController {

    /**
     * Warning! Now it works only for the localhost single-user assignment for shutting down the main Application when idle.
     * In the real serverside implementation it has to be intended for interrupting the processing.
     * <p>
     * When receiving a keep-alive 'navigator.sendBeacon()' POST signal resets the {@link #getBeaconsCount()} counter.
     */
    @PostMapping(path = "/beacon")
    @ResponseBody
    public void postBeacon(HttpSession httpSession) {
        log.trace("A keep-alive beacon from a User's browser has been received. The counter will be set to 0");
//        setBeaconsCount(0); //For Localhost use
        SessionTimerTask timerTask = getSessionBeaconsCount().get(httpSession.getId());//For serverside use
        if (timerTask != null) timerTask.setCount(0);
//TODO: to delete
        log.warn("Beacon sessionId={}", httpSession.getId());

    }
}
