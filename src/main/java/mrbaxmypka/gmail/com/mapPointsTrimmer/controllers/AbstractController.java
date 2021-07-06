package mrbaxmypka.gmail.com.mapPointsTrimmer.controllers;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import mrbaxmypka.gmail.com.mapPointsTrimmer.services.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.SessionAttributes;

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
	
	@ModelAttribute
	public void addAttributes(Model model) {
		model.addAttribute("maxFileSizeMb", maxFileSizeMb);
		model.addAttribute("serverAddress", serverAddress);
		model.addAttribute("pictograms", fileService.getPictogramsNames());
		model.addAttribute("pictogramsMap", fileService.getPictogramsNamesMap());
		log.trace("ServerAddress={}, maxFileSizeMb={}, pictograms={} attributes have been added and the 'index' page is being " +
				"returned.",
			serverAddress, maxFileSizeMb, fileService.getPictogramsNames().size());
	}
}
