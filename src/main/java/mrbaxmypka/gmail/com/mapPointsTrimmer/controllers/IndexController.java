package mrbaxmypka.gmail.com.mapPointsTrimmer.controllers;

import mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto.MultipartDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.StringWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

@Controller
public class IndexController {
	
	@Autowired
	private MessageSource messageSource;
	
	@Autowired
	private Environment environment;
	
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
