package com.example.data.domain.websocket.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.setMessageSizeLimit(10 * 1024 * 1024); // 10 MB
        registration.setSendBufferSizeLimit(10 * 1024 * 1024); // 10 MB
        registration.setSendTimeLimit(20 * 10000); // 20 sec
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {

        // server -> client
        // ex /plans/greeting (서버가 보냄)
        config.enableSimpleBroker("/client");
        // client -> server
        // ex /send/hello (클라이언트가 보냄)
        config.setApplicationDestinationPrefixes("/server");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 클라이언트에서 socket 접속을 위한 경로
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
        System.out.println("registry 연결고리" + registry);
    }


}
