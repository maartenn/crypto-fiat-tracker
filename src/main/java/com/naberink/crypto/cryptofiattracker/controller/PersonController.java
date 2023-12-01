package com.naberink.crypto.cryptofiattracker.controller;

import java.math.BigDecimal;

import com.naberink.crypto.cryptofiattracker.blockstream.BlockStreamApi;
import com.naberink.crypto.cryptofiattracker.service.PricesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequiredArgsConstructor
@Slf4j
public class PersonController {

    @GetMapping
    String getHome(Model model) {
        log.debug("Fetching home page without argument");
        model.addAttribute("address", "");
        return "home";
    }

    @GetMapping("/{address}")
    public String transactions(@PathVariable("address") String address, Model model) {
        log.debug("Fetching home page with argument: ", address);
        model.addAttribute("address", address);
        return "home";
    }
}
