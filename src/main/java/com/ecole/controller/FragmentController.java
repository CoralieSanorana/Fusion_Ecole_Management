package com.ecole.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class FragmentController {

    @GetMapping("/fragments/{role}/{page}")
    public String getFragment(@PathVariable String role, @PathVariable String page) {
        return String.format("%s/%s", role, page);
    }

    @GetMapping("/fragments/communs/{page}")
    public String getCommunFragment(@PathVariable String page) {
        return String.format("communs/%s", page);
    }

    @GetMapping("/fragments/inc/{page}")
    public String getIncFragment(@PathVariable String page) {
        return String.format("inc/%s", page);
    }
}