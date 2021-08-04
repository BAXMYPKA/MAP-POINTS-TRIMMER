package mrbaxmypka.gmail.com.mapPointsTrimmer.controllers;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import mrbaxmypka.gmail.com.mapPointsTrimmer.MapPointsTrimmerApplication;
import mrbaxmypka.gmail.com.mapPointsTrimmer.services.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.SessionAttributes;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author BAXMYPKA
 */
@Slf4j
@Getter
@Controller
@SessionAttributes(names = {"maxFileSizeMb", "serverAddress"})
public abstract class AbstractController {

    @Value("${trimmer.maxFileSizeMb}")
    private Integer maxFileSizeMb;
    @Value("${trimmer.serverAddress}")
    private String serverAddress;
    @Autowired
    private FileService fileService;
    @Autowired
    private MapPointsTrimmerApplication mapPointsTrimmerApplication;
    private final AtomicInteger beaconsCount = new AtomicInteger(0);

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

    /**
     * Checks every 15sec if a User's browser has sent the beacon keep-alive signal onto {@link BeaconController#postBeacon()}
     * and increment the {@link #beaconsCount} counter by 1. When {@link BeaconController#postBeacon()} resets it to 0 when the
     * 'navigator.sendBeacon()' is sent.
     *  When {@link #beaconsCount} > 4 the full application will be shut down by {@link MapPointsTrimmerApplication#shutDownApp()}.
     */
    protected void startBeaconTimer() {
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                System.out.println("Timer count...");
                if (beaconsCount.get() <= 4) {
                    beaconsCount.set(beaconsCount.getAndAdd(1));
                } else {
                    mapPointsTrimmerApplication.shutDownApp();
                }
            }
        };
        new Timer().scheduleAtFixedRate(timerTask, 1000, 15000);
    }

    protected int getBeaconsCount() {
        return beaconsCount.get();
    }

    protected void setBeaconsCount(int beaconsCount) {
        this.beaconsCount.set(beaconsCount);
    }
}
