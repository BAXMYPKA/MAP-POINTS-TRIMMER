package mrbaxmypka.gmail.com.mapPointsTrimmer.controllers;

import lombok.extern.slf4j.Slf4j;
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
        setBeaconsCount(0); //For Localhost use
        getSessionBeaconsCount().put(httpSession.getId(), 0); //For serverside use
    }
}
