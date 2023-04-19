package com.example.data.domain.user.repository;

import com.example.data.entity.data.User;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.testng.annotations.Test;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testSaveAndFindUser() {
        // given
        User user = User.builder()
                .password("password")
                .name("John")
                .email("john@example.com")
                .role("USER")
                .build();

        // when
        userRepository.save(user);

        // then
        User foundUser = userRepository.findById(user.getId()).orElse(null);
        assertNotNull(foundUser);
        assertEquals(user.getEmail(), foundUser.getEmail());
    }
}