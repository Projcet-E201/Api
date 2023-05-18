package com.example.data.domain;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class DataTime {
    public String timeToUTC(Object pretime) {
        String timeString = String.valueOf(pretime);
        LocalDateTime time = LocalDateTime.parse(timeString, DateTimeFormatter.ofPattern("yyyy-MM-dd/HH:mm:ss.SSS"));
        long epochSeconds = time.toEpochSecond(ZoneOffset.UTC);
        return Instant.ofEpochSecond(epochSeconds).toString();
    }
}
