package com.example.data.config;


import com.influxdb.client.*;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;


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
        OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder()
                .connectTimeout(40, TimeUnit.SECONDS)       // 모두 default 10
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS);

        InfluxDBClientOptions options = InfluxDBClientOptions.builder()
                .url(url)
                .authenticateToken(token.toCharArray())
                .okHttpClient(okHttpClientBuilder)
                .build();

        return InfluxDBClientFactory.create(options);
    }
}

