package com.example.data.config;


import com.influxdb.client.*;
import com.influxdb.client.write.events.BackpressureEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;


@Configuration
public class InfluxDBConfig {

    @Value("${spring.influxdb.url}")
    private String url;

    @Value("${spring.influxdb.username}")
    private String username;

    @Value("${spring.influxdb.password}")
    private String password;

    @Value("${spring.influxdb.token}")
    private String token;



    @Bean
    public InfluxDBClient influxDBClient() {
        InfluxDBClientOptions options = InfluxDBClientOptions.builder()
                .url(url)
                .authenticateToken(token.toCharArray())
                .build();

        return InfluxDBClientFactory.create(options);
    }
}
