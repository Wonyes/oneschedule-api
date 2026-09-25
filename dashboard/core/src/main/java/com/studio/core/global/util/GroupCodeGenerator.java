package com.studio.core.global.util;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class GroupCodeGenerator {

    public String generate() {
        return UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();
    }
}