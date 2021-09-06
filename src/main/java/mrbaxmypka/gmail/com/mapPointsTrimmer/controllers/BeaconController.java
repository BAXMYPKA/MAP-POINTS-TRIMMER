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
     * Periodic signal that an end user is still keep in touch with the server.
     * When idle for a long time it is the localhost single-user assignment for shutting down the main Application.
     * In the real serverside implementation it has to be intended for interrupting the processing and deleting the
     * resulting temp file as the end user won't be able to obtain it.
     * <p>
     * When receiving a keep-alive 'navigator.sendBeacon()' POST signal resets the {@link #getWebSessionService()} counter.
     */
    @PostMapping(path = "/beacon")
    @ResponseBody
    public void postBeacon(HttpSession httpSession) {
        log.trace("A keep-alive beacon from a User's browser has been received. The counter will be set to 0");
        getWebSessionService().postBeacon(httpSession.getId());
    }

    /**
     * When closing a browser, closing a tab with the Application or refresh a page
     * the browser sends a 'navigator.sendBeacon('/stop')' POST signal for the server to stop the processing
     * and delete the resulting temp file as the end User won't be able to obtain it.
     */
    @PostMapping(path = "/stop")
    @ResponseBody
    public void postStopBeacon(HttpSession httpSession) {
        log.trace("A refresh or close tab browser event has been received to stop the file processing for the session={}",
                httpSession.getId());
        getWebSessionService().postStopBeacon(httpSession.getId());
    }
}
