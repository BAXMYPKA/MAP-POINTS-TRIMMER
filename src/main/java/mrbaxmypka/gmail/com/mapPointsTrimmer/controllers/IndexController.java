package mrbaxmypka.gmail.com.mapPointsTrimmer.controllers;

import lombok.extern.slf4j.Slf4j;
import mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto.MultipartDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.net.UnknownHostException;

@Slf4j
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
		log.debug("ServerAddress={}, maxFileSizeMb={} attributes have been added and the 'index' page is being returned.",
			serverAddress, maxFileSizeMb);
		return "index";
	}
	
}
