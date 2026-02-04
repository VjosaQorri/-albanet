package com.example.albanet.catalog.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CatalogViewController {

    @GetMapping("/catalog")
    public String catalog() {
        return "client/catalog";
    }
}
