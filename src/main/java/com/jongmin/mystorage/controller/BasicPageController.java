package com.jongmin.mystorage.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BasicPageController {
	@GetMapping("/upload")
	public String uploadPage() {
		return "upload";
	}
}
