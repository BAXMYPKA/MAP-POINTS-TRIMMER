package mrbaxmypka.gmail.com.mapPointsTrimmer.controllers;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import mrbaxmypka.gmail.com.mapPointsTrimmer.MapPointsTrimmerApplication;
import mrbaxmypka.gmail.com.mapPointsTrimmer.services.FileService;
import mrbaxmypka.gmail.com.mapPointsTrimmer.services.MultipartFileService;
import mrbaxmypka.gmail.com.mapPointsTrimmer.utils.SessionTimerTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.SessionAttributes;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author BAXMYPKA
 */
@Slf4j
@Getter
@Controller
@SessionAttributes(names = {"maxFileSizeMb", "serverAddress"})
public abstract class AbstractController {
	
	/**
	 * This 'final static' implementation is ONLY for localhost using!
	 * The real server implementation has to have a local thread variable distiguished by SESSIONID.
	 */
	private final static AtomicInteger beaconsCount = new AtomicInteger(0);
	@Getter(AccessLevel.PROTECTED)
	private final static Map<String, SessionTimerTask> sessionBeaconsCount = new ConcurrentHashMap<>(2);
	@Getter(AccessLevel.PROTECTED)
	private final static ScheduledThreadPoolExecutor timer = new ScheduledThreadPoolExecutor(10);
	@Value("${trimmer.maxFileSizeMb}")
	private Integer maxFileSizeMb;
	@Value("${trimmer.serverAddress}")
	private String serverAddress;
	@Autowired
	private FileService fileService;
	@Autowired
	private MultipartFileService multipartFileService;
	@Autowired
	private MapPointsTrimmerApplication mapPointsTrimmerApplication;
	
	@ModelAttribute
	public void addAttributes(Model model) {
		if (!model.containsAttribute("maxFileSizeMb")) model.addAttribute("maxFileSizeMb", maxFileSizeMb);
		if (!model.containsAttribute("serverAddress")) model.addAttribute("serverAddress", serverAddress);
		if (!model.containsAttribute("pictograms")) model.addAttribute("pictograms", fileService.getPictogramsNames());
		if (!model.containsAttribute("pictogramsMap"))
			model.addAttribute("pictogramsMap", fileService.getPictogramsNamesPaths());
		log.trace("ServerAddress={}, maxFileSizeMb={}, pictograms={} attributes have been added and the 'index' page is being " +
					"returned.",
			  serverAddress, maxFileSizeMb, fileService.getPictogramsNamesPaths().size());
	}
	
	//TODO: TO REMAKE IT AS THE SESSION-ORIENTED WHEN SERVER-SIDE
	
	/**
	 * Single-user localhost-oriented method!
	 * Checks every 15sec if a User's browser has sent the beacon keep-alive signal onto {@link BeaconController#postBeacon(HttpSession)}
	 * and increment the {@link #beaconsCount} counter by 1. When {@link BeaconController#postBeacon(HttpSession)} resets it to 0 when the
	 * 'navigator.sendBeacon()' is sent.
	 * When {@link #getBeaconsCount()} > 4 the full application will be shut down by {@link MapPointsTrimmerApplication#shutDownApp()}.
	 */
	protected void startGlobalBeaconTimer() {
		TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
				if (getBeaconsCount() <= 3) {
					setBeaconsCount(getBeaconsCount() + 1);
				} else {
					log.warn("Timer count = {} so the App is being shut down...", getBeaconsCount());
					mapPointsTrimmerApplication.shutDownApp();
				}
			}
		};
		new Timer().scheduleAtFixedRate(timerTask, 1000, 10000);
	}
	
	/**
	 * Real serverside multiuser-oriented method!
	 * Checks every 15sec if a User's browser has sent the beacon keep-alive signal onto {@link BeaconController#postBeacon(HttpSession)}
	 * and increment the {@link #sessionBeaconsCount} counter by 1 only for the current {@link HttpSession}.
	 * {@link BeaconController#postBeacon(HttpSession)} resets it to 0 when the 'navigator.sendBeacon()' is sent.
	 * When {@link #sessionBeaconsCount)} > 4 the appropriate process and the temp file for this session will be killed be
	 * {@link MultipartFileService#deleteTempFile(String)}.
	 */
	protected void startSessionBeaconTimer(String sessionId) {

		//TODO: to delete
		log.warn("Abstract start timer for sessionId={}", sessionId);
		
		SessionTimerTask timerTask = sessionBeaconsCount.get(sessionId);
        if (timerTask == null || timerTask.getCount() > 3) {
            SessionTimerTask oldTask =
                  sessionBeaconsCount.put(sessionId, timerTask = new SessionTimerTask(sessionId, timer, sessionBeaconsCount));
            if (oldTask != null) oldTask.cancel();
        } else {
            timerTask.setCount(0);
            return;
        }
		ScheduledFuture<?> scheduledFuture = timer.scheduleAtFixedRate(timerTask, 1, 10, TimeUnit.SECONDS);
	}
	
	protected int getBeaconsCount() {
		return beaconsCount.get();
	}
	
	protected void setBeaconsCount(int newBeaconCountVal) {
		log.trace("BeaconsCount will be set to {}", newBeaconCountVal);
		beaconsCount.set(newBeaconCountVal);
	}
}
