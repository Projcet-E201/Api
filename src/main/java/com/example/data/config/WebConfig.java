package com.example.data.config;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
public class WebConfig {
    @Configuration
    public class WebMvcConfig implements WebMvcConfigurer {
        @Override
        public void addCorsMappings(CorsRegistry registry) {
            System.out.println("config - corsMapping");

            registry.addMapping("/**")
                    .allowedMethods(CorsConfiguration.ALL)
                    .allowedHeaders(CorsConfiguration.ALL)
                    .allowedOrigins("http://localhost:3000", "http://localhost:80", "https://k8e201.p.ssafy.io", "https://semse.info")
                    .allowedOriginPatterns(CorsConfiguration.ALL)
//                .allowedOriginPatterns(CorsConfiguration.ALL, "*")
                    .allowCredentials(true);
        }
    }
}
