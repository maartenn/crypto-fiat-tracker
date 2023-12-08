package com.naberink.crypto.cryptofiattracker.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
public class PersonController {

    @GetMapping
    String getHome(Model model) {
        log.debug("Fetching home page without argument");
        model.addAttribute("addresses", List.of(""));
        return "home";
    }

    @GetMapping("/{addresses}")
    public String transactions(@PathVariable("addresses") String address, Model model) {
        log.debug("Fetching home page with argument: ", address);

        model.addAttribute("addresses", Arrays.stream(address.split(",")).toList());
        return "home";
    }


}
