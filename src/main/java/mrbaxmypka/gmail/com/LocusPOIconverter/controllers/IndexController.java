package mrbaxmypka.gmail.com.LocusPOIconverter.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {
	
	@GetMapping(path = {"/", "/converter"})
	public String getIndex() {
		System.out.println("get index");
		return "index";
	}
	
}
