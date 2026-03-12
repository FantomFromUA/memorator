package com.memorator.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.memorator.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByLogin(String login);

    Optional<User> findByEmailOrLogin(String email, String login);

    boolean existsByEmail(String email);

    boolean existsByLogin(String login);
}