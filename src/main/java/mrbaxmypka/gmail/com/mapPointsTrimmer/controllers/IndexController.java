package mrbaxmypka.gmail.com.mapPointsTrimmer.controllers;

import mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto.MultipartDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.net.UnknownHostException;

@Controller
public class IndexController {
	
	@Value("${trimmer.maxFileSizeMb}")
	private Integer maxFileSizeMb;
	
	@Value("${trimmer.serverAddress}")
	private String serverAddress;
	
	@GetMapping(path = {"/", "index", "/trimmer"})
	public String getIndex(Model model) throws UnknownHostException {
		model.addAttribute("poiFile", new MultipartDto());
		model.addAttribute("maxFileSizeMb", maxFileSizeMb);
		model.addAttribute("serverAddress", serverAddress);
		return "index";
	}
	
}
