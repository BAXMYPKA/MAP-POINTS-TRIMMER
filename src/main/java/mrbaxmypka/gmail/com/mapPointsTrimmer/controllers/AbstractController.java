package mrbaxmypka.gmail.com.mapPointsTrimmer.controllers;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
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
	
	@ModelAttribute
	public void addAttributes(Model model) {
		model.addAttribute("maxFileSizeMb", maxFileSizeMb);
		model.addAttribute("serverAddress", serverAddress);
		log.trace("ServerAddress={}, maxFileSizeMb={} attributes have been added and the 'index' page is being " +
				"returned.",
			serverAddress, maxFileSizeMb);
	}
}
