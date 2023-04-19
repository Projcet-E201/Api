package com.example.data.entity.data;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;


public class UserTest {
    
    @Test
    public void testUserCreation() {
        User user = User.builder()
//                .id(1L)
                .password("testpassword")
                .name("testuser")
                .email("testuser@example.com")
                .role("user")
                .build();

        assertNotNull(user.getId());
//        System.out.println("user.getAlarms() = " + user.getAlarms());
        assertEquals("testpassword", user.getPassword());
        assertEquals("testuser", user.getName());
        assertEquals("testuser@example.com", user.getEmail());
        assertEquals("user", user.getRole());
//        assertNotNull(user.getAlarms());
    }
}




