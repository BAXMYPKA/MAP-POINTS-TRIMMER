package mrbaxmypka.gmail.com.mapPointsTrimmer.controllers;

import lombok.extern.slf4j.Slf4j;
import mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto.MultipartDto;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.net.UnknownHostException;

@Slf4j
@Controller
public class IndexController extends AbstractController {
	
	@GetMapping(path = {"/", "index", "/trimmer"})
	public String getIndex(Model model) throws UnknownHostException {
		model.addAttribute("poiFile", new MultipartDto());
		log.debug("Attribute 'poiFile' as the new '{}' has been added and the 'index' page is being returned.",
			MultipartDto.class.getSimpleName());
		return "index";
	}
	
}
