package mrbaxmypka.gmail.com.mapPointsTrimmer.controllers;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import mrbaxmypka.gmail.com.mapPointsTrimmer.services.FileService;
import mrbaxmypka.gmail.com.mapPointsTrimmer.services.WebSessionService;
import mrbaxmypka.gmail.com.mapPointsTrimmer.utils.DistanceUnits;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.SessionAttributes;

/**
 * @author BAXMYPKA
 */
@Slf4j
@Getter(AccessLevel.PROTECTED)
@Controller
@SessionAttributes(names = {"maxFileSizeMb", "serverAddress"})
public abstract class AbstractController {
	
	@Value("${trimmer.maxFileSizeMb}")
	private Integer maxFileSizeMb;
	@Value("${trimmer.serverAddress}")
	private String serverAddress;
	@Value("${trimmer.single-user-mode:true}")
	private boolean singleUserMode;
	@Value("${trimmer.supportedZipEncodings}")
	private String supportedZipEncodings;
	@Autowired
	private FileService fileService;
	@Autowired
	private WebSessionService webSessionService;
	@Autowired
	private MessageSource messageSource;
	
	@ModelAttribute
	public void addAttributes(Model model) {
		if (!model.containsAttribute("singleUserMode")) model.addAttribute("singleUserMode", singleUserMode);
		if (!model.containsAttribute("maxFileSizeMb")) model.addAttribute("maxFileSizeMb", maxFileSizeMb);
		if (!model.containsAttribute("serverAddress")) model.addAttribute("serverAddress", serverAddress);
		if (!model.containsAttribute("pictograms")) model.addAttribute("pictograms", fileService.getPictogramsNames());
		if (!model.containsAttribute("pictogramsMap"))
			model.addAttribute("pictogramsMap", fileService.getPictogramsNamesPaths());
		if (!model.containsAttribute("zipEncodings"))
			model.addAttribute("zipEncodings", supportedZipEncodings.split(","));
		if (!model.containsAttribute("distanceUnits")) {
			model.addAttribute("distanceUnits", DistanceUnits.values());
		}
		log.trace("ServerAddress={}, maxFileSizeMb={}, pictograms={} attributes have been added and the 'index' page is being " +
						"returned.",
				serverAddress, maxFileSizeMb, fileService.getPictogramsNamesPaths().size());
	}
}
