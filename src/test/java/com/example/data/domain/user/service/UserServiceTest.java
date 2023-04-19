package com.example.data.domain.user.service;

import com.example.data.domain.user.repository.UserRepository;
import com.example.data.entity.data.User;
import com.example.data.domain.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserServiceTest {

    @Autowired
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Test
    void join() {
        // given
        User user = User.builder()
                .name("test user")
                .email("test@example.com")
                .password("test password")
                .role("ROLE_USER")
                .build();

        Mockito.when(userRepository.findByName(user.getName()))
                .thenReturn(Arrays.asList(user));

        // when
        Long savedUserId = userService.join(user);

        // then
        assertThat(savedUserId).isEqualTo(user.getId());
    }

    @Test
    void join_duplicate() {
        // given
        User user1 = User.builder()
                .name("test user")
                .email("test@example.com")
                .password("test password")
                .role("ROLE_USER")
                .build();

        User user2 = User.builder()
                .name("test user")
                .email("test2@example.com")
                .password("test password")
                .role("ROLE_USER")
                .build();

        Mockito.when(userRepository.findByName(user1.getName()))
                .thenReturn(Arrays.asList(user1));

        // when, then
        assertThrows(IllegalStateException.class, () -> userService.join(user2));
    }
}