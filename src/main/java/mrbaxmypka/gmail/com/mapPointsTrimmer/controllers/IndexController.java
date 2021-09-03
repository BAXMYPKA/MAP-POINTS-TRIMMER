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
        log.debug("Index page is being returned for sessionId={}.", httpSession.getId());
        getWebSessionService().startSessionBeaconTimer(httpSession.getId()); //To start counting received keep-alive POST signals
        return "index";
    }

}
