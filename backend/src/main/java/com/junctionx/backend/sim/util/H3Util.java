package com.junctionx.backend.sim.util;

import com.uber.h3core.H3Core;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

@Component
public class H3Util {
    private final H3Core h3;

    public H3Util() throws IOException {
        this.h3 = H3Core.newInstance();
    }

    /** Returns center hex + neighbors up to distance k, preserving order without duplicates. */
    public List<String> kRings(String centerHex, int k) {
        if (centerHex == null || centerHex.isBlank()) return List.of();
        Set<String> set = new LinkedHashSet<>();
        set.add(centerHex);
        for (int i = 1; i <= k; i++) set.addAll(h3.kRing(centerHex, i));
        return new ArrayList<>(set);
    }
}
