//package com.example.data.domain.websocket.config;
//
//import org.springframework.stereotype.Component;
//import org.springframework.web.socket.WebSocketSession;
//
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//@Component
//public class WebSocketSessionManager {
//    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
//
//    public void addSession(WebSocketSession session) {
//        sessions.put(session.getId(), session);
//    }
//
//    public void removeSession(WebSocketSession session) {
//        sessions.remove(session.getId());
//    }
//
//    public boolean hasActiveSessions() {
//        return !sessions.isEmpty();
//    }
//}
