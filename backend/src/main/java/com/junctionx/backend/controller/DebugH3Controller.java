package com.junctionx.backend.controller;

import com.junctionx.backend.sim.util.H3Util;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/debug/h3")
public class DebugH3Controller {
    private final H3Util h3;
    public DebugH3Controller(H3Util h3) { this.h3 = h3; }

    @GetMapping("/kring")
    public List<String> kRing(@RequestParam String hex, @RequestParam(defaultValue = "2") int k) {
        return h3.kRings(hex, k);
    }
}
