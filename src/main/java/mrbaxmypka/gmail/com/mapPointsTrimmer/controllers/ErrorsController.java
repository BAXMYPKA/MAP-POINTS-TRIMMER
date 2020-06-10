package mrbaxmypka.gmail.com.mapPointsTrimmer.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.Locale;

@Controller
public class ErrorsController {
	
	@GetMapping(path = "/error")
	public ModelAndView getErrorPage(Model model, Locale locale) {
		return new ModelAndView("error");
	}
}
