package com.example.jwtlesson.repository;

import com.example.jwtlesson.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @EntityGraph(attributePaths = "roles")
    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    @Override
    @EntityGraph(attributePaths = "roles")
    List<User> findAll();
}
