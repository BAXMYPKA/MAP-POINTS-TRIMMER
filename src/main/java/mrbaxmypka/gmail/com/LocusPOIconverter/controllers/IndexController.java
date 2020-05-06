package mrbaxmypka.gmail.com.LocusPOIconverter.controllers;

import mrbaxmypka.gmail.com.LocusPOIconverter.entitiesDto.MultipartDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Locale;

@Controller
public class IndexController {
	
	@Autowired
	private MessageSource messageSource;
	
	@GetMapping(path = {"/", "/converter"})
	public String getIndex(Model model, Locale locale) {
		String downloadMessage = messageSource.getMessage("userMessage.downloadMessageAwait", null, locale);
		model.addAttribute("poiFile", new MultipartDto());
		model.addAttribute("downloadMessage", downloadMessage);
		return "index";
	}
	
}
