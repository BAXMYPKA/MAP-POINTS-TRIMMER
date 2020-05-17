package mrbaxmypka.gmail.com.mapPointsTrimmer.controllers;

import mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto.MultipartDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {
	
	@Autowired
	private MessageSource messageSource;
	
	@Value("${kml.maxFileSizeMb}")
	private Integer maxFileSizeMb;
	
	@GetMapping(path = {"/", "index", "/trimmer"})
	public String getIndex(Model model) {
		model.addAttribute("poiFile", new MultipartDto());
		model.addAttribute("maxFileSizeMb", maxFileSizeMb);
		return "index";
	}
	
}
