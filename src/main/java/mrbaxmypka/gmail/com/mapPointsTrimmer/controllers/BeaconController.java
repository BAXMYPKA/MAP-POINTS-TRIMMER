package mrbaxmypka.gmail.com.mapPointsTrimmer.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;

@Slf4j
@Controller
public class BeaconController extends AbstractController {

	/**
	 * When receiving a keep-alive 'navigator.sendBeacon()' POST signal resets the {@link #getBeaconsCount()} counter.
	 */
	@PostMapping(path = "/beacon")
	@ResponseBody
	public void postBeacon() {
		log.trace("A keep-alive beacon from a User's browser has been received. The counter will be set to 0");

		//TODO: to delete
		log.warn("BEACON THREAD = "+Thread.currentThread().getName()+" ID = "+Thread.currentThread().getId());
		log.warn("BEACON SESSIONID = "+ RequestContextHolder.currentRequestAttributes().getSessionId());

		setBeaconsCount(0);
	}
}
