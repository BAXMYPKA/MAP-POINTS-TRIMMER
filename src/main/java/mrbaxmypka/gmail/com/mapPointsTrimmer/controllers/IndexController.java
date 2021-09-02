package mrbaxmypka.gmail.com.mapPointsTrimmer.controllers;

import lombok.extern.slf4j.Slf4j;
import mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto.MultipartDto;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpSession;

@Slf4j
@Controller
public class IndexController extends AbstractController {

    @GetMapping(path = {"/", "index", "/trimmer"})
    public String getIndex(Model model, HttpSession httpSession) {
        model.addAttribute("poiFile", new MultipartDto());
        log.debug("Attribute 'poiFile' as the new '{}' has been added and the 'index' page is being returned.",
                MultipartDto.class.getSimpleName());

        //TODO: to make as trace
        log.warn("Single-user-mode={}, sessionId={}", isSingleUserMode(), httpSession.getId());

        getWebSessionService().startSessionBeaconTimer(httpSession.getId()); //To start counting received keep-alive POST signals
        return "index";
    }

}
