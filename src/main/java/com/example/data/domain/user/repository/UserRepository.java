package com.example.data.domain.user.repository;

import com.example.data.entity.data.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    // select m from User m where u.name = ?
    List<User> findByName(String name);
}
